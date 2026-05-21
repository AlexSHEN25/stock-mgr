package co.handk.backend.service.impl;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.context.UserContext;
import co.handk.backend.entity.Dept;
import co.handk.backend.entity.User;
import co.handk.backend.exception.AccessDeniedException;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.mapper.UserMapper;
import co.handk.backend.service.DeptService;
import co.handk.backend.service.PermissionQueryService;
import co.handk.backend.service.UserService;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.dto.create.CreateUserDTO;
import co.handk.common.model.dto.query.UserQueryDTO;
import co.handk.common.model.dto.update.ChangePasswordDTO;
import co.handk.common.model.dto.update.UpdateUserDTO;
import co.handk.common.model.vo.UserVO;
import co.handk.common.util.PasswordUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User, UserVO>
        implements UserService {

    private final DeptService deptService;
    private final PermissionQueryService permissionQueryService;

    @Override
    protected UserVO toVO(User entity) {
        if (entity == null) {
            return null;
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(entity, vo);
        if (entity.getDeptId() != null) {
            Dept dept = deptService.getByIdNotDeleted(entity.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getName());
            }
        }
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
    public <C> boolean saveByDto(C dto) {
        if (dto instanceof CreateUserDTO createDto) {
            String salt = PasswordUtil.generateSalt();
            createDto.setPassword(PasswordUtil.encrypt(createDto.getPassword(), salt));
            User entity = toEntity(createDto);
            entity.setSalt(salt);
            return this.save(entity);
        }
        return super.saveByDto(dto);
    }

    @Override
    public <U> boolean updateByDto(U dto) {
        if (dto instanceof UpdateUserDTO updateUserDTO) {
            Long currentUserId = UserContext.getUserIdOrDefault();
            boolean isSuperAdmin = permissionQueryService.isSuperAdmin(currentUserId);
            if (!isSuperAdmin) {
                if (!currentUserId.equals(updateUserDTO.getId())) {
                    throw new AccessDeniedException(
                            MessageKeyConstant.ERROR_NO_PERMISSION,
                            "他ユーザー情報を変更する権限がありません"
                    );
                }
                if (updateUserDTO.getDeptId() != null || updateUserDTO.getStatus() != null) {
                    throw new AccessDeniedException(
                            MessageKeyConstant.ERROR_NO_PERMISSION,
                            "部署・ステータスの変更は管理者のみ可能です"
                    );
                }
            }
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
