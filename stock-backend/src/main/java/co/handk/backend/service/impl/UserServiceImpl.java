package co.handk.backend.service.impl;

import co.handk.backend.config.AvatarStorageProperties;
import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.context.UserContext;
import co.handk.backend.entity.Dept;
import co.handk.backend.entity.Role;
import co.handk.backend.entity.User;
import co.handk.backend.entity.UserRole;
import co.handk.backend.exception.AccessDeniedException;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.mapper.RoleMapper;
import co.handk.backend.mapper.UserMapper;
import co.handk.backend.mapper.UserRoleMapper;
import co.handk.backend.service.DeptService;
import co.handk.backend.service.PermissionQueryService;
import co.handk.backend.service.UserService;
import co.handk.common.constant.FieldNameConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.dto.create.CreateUserDTO;
import co.handk.common.model.dto.query.UserQueryDTO;
import co.handk.common.model.dto.update.ChangePasswordDTO;
import co.handk.common.model.dto.update.UpdateUserDTO;
import co.handk.common.model.vo.UserVO;
import co.handk.common.util.PasswordUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User, UserVO>
        implements UserService {

    private static final String DEFAULT_AVATAR_PATH = "/avatar/default-avatar.svg";
    private static final String AVATAR_UPLOAD_URI_PREFIX = "/avatar/upload/";
    private static final int MAX_AVATAR_LENGTH = 64;

    private final DeptService deptService;
    private final PermissionQueryService permissionQueryService;
    private final AvatarStorageProperties avatarStorageProperties;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    @Override
    protected UserVO toVO(User entity) {
        if (entity == null) {
            return null;
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(entity, vo);
        if (!StringUtils.hasText(vo.getAvatar())) {
            vo.setAvatar(DEFAULT_AVATAR_PATH);
        }
        if (entity.getDeptId() != null) {
            Dept dept = deptService.getByIdNotDeleted(entity.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getName());
            }
        }
        fillRoleInfo(entity.getId(), vo);
        return vo;
    }

    @Override
    protected <D> User toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        User entity = new User();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        if (dto instanceof CreateUserDTO createDto) {
            String salt = PasswordUtil.generateSalt();
            createDto.setPassword(PasswordUtil.encrypt(createDto.getPassword(), salt));
            createDto.setAvatar(normalizeAvatarForSave(createDto.getAvatar(), null));

            User entity = toEntity(createDto);
            entity.setSalt(salt);
            boolean saved = this.save(entity);
            if (!saved) {
                return false;
            }
            syncUserRole(entity.getId(), createDto.getRoleId());
            return true;
        }
        return super.saveByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        if (dto instanceof UpdateUserDTO updateUserDTO) {
            User existed = this.getByIdNotDeleted(updateUserDTO.getId());
            if (existed == null) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "ユーザーが存在しません");
            }
            Long currentUserId = UserContext.getUserIdOrDefault();
            boolean isSuperAdmin = permissionQueryService.isSuperAdmin(currentUserId);
            if (!isSuperAdmin) {
                if (!currentUserId.equals(updateUserDTO.getId())) {
                    throw new AccessDeniedException(
                            MessageKeyConstant.ERROR_NO_PERMISSION,
                            "他ユーザー情報を変更する権限がありません"
                    );
                }
                if (updateUserDTO.getDeptId() != null || updateUserDTO.getStatus() != null || updateUserDTO.getRoleId() != null) {
                    throw new AccessDeniedException(
                            MessageKeyConstant.ERROR_NO_PERMISSION,
                            "部署・ロール・ステータスの変更は管理者のみ可能です"
                    );
                }
            }

            String newPlainPassword = null;
            if (StringUtils.hasText(updateUserDTO.getPassword())) {
                newPlainPassword = updateUserDTO.getPassword().trim();
            }
            // Password is optional in edit form; leave empty means keep current password.
            updateUserDTO.setPassword(null);
            updateUserDTO.setAvatar(normalizeAvatarForSave(updateUserDTO.getAvatar(), existed.getAvatar()));
            boolean updated = super.updateByDto(dto);
            if (updated && updateUserDTO.getRoleId() != null) {
                syncUserRole(updateUserDTO.getId(), updateUserDTO.getRoleId());
            }
            if (updated && StringUtils.hasText(newPlainPassword)) {
                String salt = StringUtils.hasText(existed.getSalt()) ? existed.getSalt() : PasswordUtil.generateSalt();
                String encrypted = PasswordUtil.encrypt(newPlainPassword, salt);
                this.lambdaUpdate()
                        .eq(User::getId, updateUserDTO.getId())
                        .eq(User::getDeleted, DeleteEnum.UNDELETED.getCode())
                        .set(User::getSalt, salt)
                        .set(User::getPassword, encrypted)
                        .update();
            }
            return updated;
        }
        return super.updateByDto(dto);
    }

    @Override
    public boolean changePassword(Long userId, ChangePasswordDTO dto) {
        Long currentUserId = UserContext.getUserIdOrDefault();
        boolean isSuperAdmin = permissionQueryService.isSuperAdmin(currentUserId);
        if (!isSuperAdmin && !currentUserId.equals(userId)) {
            throw new AccessDeniedException(
                    MessageKeyConstant.ERROR_USER_PASSWORD_SELF_ONLY,
                    "自分のパスワードのみ変更できます"
            );
        }

        User existed = this.getByIdNotDeleted(userId);
        if (existed == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "ユーザーが存在しません");
        }

        String salt = StringUtils.hasText(existed.getSalt()) ? existed.getSalt() : PasswordUtil.generateSalt();
        existed.setSalt(salt);
        existed.setPassword(PasswordUtil.encrypt(dto.getPassword(), salt));
        return this.updateById(existed);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(Long userId, MultipartFile file) {
        User existed = this.getByIdNotDeleted(userId);
        if (existed == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "ユーザーが存在しません");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "アップロードファイルは必須です");
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "画像ファイルのみアップロード可能です");
        }

        String ext = resolveExtension(file.getOriginalFilename());
        String fileName = buildShortAvatarFileName(ext);
        try {
            Path uploadDir = resolveAvatarStoreDir();
            Files.createDirectories(uploadDir);
            Path target = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            String avatarPath = AVATAR_UPLOAD_URI_PREFIX + fileName;
            if (avatarPath.length() > MAX_AVATAR_LENGTH) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "アバターの保存パスが長すぎます");
            }

            deleteOldAvatarIfNeeded(existed.getAvatar());
            existed.setAvatar(avatarPath);
            this.updateById(existed);
            return avatarPath;
        } catch (IOException e) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "アバターの保存に失敗しました", e);
        }
    }

    private void fillRoleInfo(Long userId, UserVO vo) {
        if (userId == null || vo == null) {
            return;
        }
        UserRole userRole = userRoleMapper.selectOne(new QueryWrapper<UserRole>()
                .eq("user_id", userId)
                .eq(FieldNameConstant.COLUMN_DELETED, DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
        if (userRole == null || userRole.getRoleId() == null) {
            return;
        }
        vo.setRoleId(userRole.getRoleId());
        Role role = roleMapper.selectOne(new QueryWrapper<Role>()
                .eq(FieldNameConstant.COLUMN_ID, userRole.getRoleId())
                .eq(FieldNameConstant.COLUMN_DELETED, DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
        if (role != null) {
            vo.setRoleName(role.getName());
        }
    }

    private void syncUserRole(Long userId, Long roleId) {
        if (userId == null || roleId == null) {
            return;
        }
        List<UserRole> existedList = userRoleMapper.selectList(new QueryWrapper<UserRole>()
                .eq("user_id", userId)
                .eq(FieldNameConstant.COLUMN_DELETED, DeleteEnum.UNDELETED.getCode()));
        if (existedList == null || existedList.isEmpty()) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
            return;
        }
        UserRole keeper = existedList.get(0);
        userRoleMapper.update(null, new UpdateWrapper<UserRole>()
                .eq(FieldNameConstant.COLUMN_ID, keeper.getId())
                .eq(FieldNameConstant.COLUMN_DELETED, DeleteEnum.UNDELETED.getCode())
                .set("role_id", roleId));

        if (existedList.size() > 1) {
            List<Long> redundantIds = existedList.stream().skip(1).map(UserRole::getId).toList();
            userRoleMapper.update(null, new UpdateWrapper<UserRole>()
                    .in(FieldNameConstant.COLUMN_ID, redundantIds)
                    .eq(FieldNameConstant.COLUMN_DELETED, DeleteEnum.UNDELETED.getCode())
                    .set(FieldNameConstant.COLUMN_DELETED, DeleteEnum.DELETED.getCode()));
        }
    }

    private String normalizeAvatarForSave(String incomingAvatar, String fallbackAvatar) {
        String fallback = StringUtils.hasText(fallbackAvatar) ? fallbackAvatar : DEFAULT_AVATAR_PATH;
        if (!StringUtils.hasText(incomingAvatar)) {
            return fallback;
        }
        String trimmed = incomingAvatar.trim();
        if (trimmed.startsWith("data:image/")) {
            String uploadedPath = saveBase64Avatar(trimmed);
            return StringUtils.hasText(uploadedPath) ? uploadedPath : fallback;
        }
        if (trimmed.length() > MAX_AVATAR_LENGTH) {
            return fallback;
        }
        return trimmed;
    }

    private String saveBase64Avatar(String dataUri) {
        try {
            int comma = dataUri.indexOf(',');
            if (comma <= 0 || comma >= dataUri.length() - 1) {
                return null;
            }
            String header = dataUri.substring(0, comma).toLowerCase();
            String payload = dataUri.substring(comma + 1);
            String ext = ".png";
            if (header.contains("image/jpeg") || header.contains("image/jpg")) {
                ext = ".jpg";
            } else if (header.contains("image/webp")) {
                ext = ".webp";
            } else if (header.contains("image/gif")) {
                ext = ".gif";
            } else if (header.contains("image/svg+xml")) {
                ext = ".svg";
            }
            byte[] bytes = Base64.getDecoder().decode(payload);
            if (bytes.length == 0) {
                return null;
            }
            Path uploadDir = resolveAvatarStoreDir();
            Files.createDirectories(uploadDir);
            String fileName = buildShortAvatarFileName(ext);
            Path target = uploadDir.resolve(fileName);
            Files.write(target, bytes);
            String avatarPath = AVATAR_UPLOAD_URI_PREFIX + fileName;
            if (avatarPath.length() > MAX_AVATAR_LENGTH) {
                Files.deleteIfExists(target);
                return null;
            }
            return avatarPath;
        } catch (Exception e) {
            return null;
        }
    }

    private String buildShortAvatarFileName(String ext) {
        String token = Long.toString(System.currentTimeMillis(), 36);
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return token + random + ext;
    }

    private String resolveExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return ".png";
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) {
            return ".png";
        }
        String ext = originalFilename.substring(dot).toLowerCase();
        if (ext.length() > 10) {
            return ".png";
        }
        return ext;
    }

    private void deleteOldAvatarIfNeeded(String oldPath) {
        if (!StringUtils.hasText(oldPath) || !oldPath.startsWith(AVATAR_UPLOAD_URI_PREFIX)) {
            return;
        }
        String oldFileName = oldPath.substring(AVATAR_UPLOAD_URI_PREFIX.length());
        if (!StringUtils.hasText(oldFileName)) {
            return;
        }
        try {
            Files.deleteIfExists(resolveAvatarStoreDir().resolve(oldFileName));
        } catch (IOException ignored) {
        }
    }

    private Path resolveAvatarStoreDir() {
        return avatarStorageProperties.getRootDir().resolve("upload");
    }

    @Override
    protected <Q> void buildJoinConditions(Q dto, QueryWrapper<User> wrapper) {
        if (!(dto instanceof UserQueryDTO queryDTO)) {
            return;
        }
        if (StringUtils.hasText(queryDTO.getDeptName())) {
            List<Long> deptIds = deptService.lambdaQuery()
                    .eq(Dept::getDeleted, DeleteEnum.UNDELETED.getCode())
                    .like(Dept::getName, queryDTO.getDeptName().trim())
                    .list()
                    .stream()
                    .map(Dept::getId)
                    .toList();
            if (deptIds.isEmpty()) {
                wrapper.eq("id", -1L);
            } else {
                wrapper.in("dept_id", deptIds);
            }
        }
    }
}
