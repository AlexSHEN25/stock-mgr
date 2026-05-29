package co.handk.backend.service.impl;

import co.handk.backend.constant.SecurityConstant;
import co.handk.backend.entity.Permission;
import co.handk.backend.entity.Role;
import co.handk.backend.entity.RolePermission;
import co.handk.backend.entity.UserRole;
import co.handk.backend.mapper.PermissionMapper;
import co.handk.backend.mapper.RoleMapper;
import co.handk.backend.mapper.RolePermissionMapper;
import co.handk.backend.mapper.UserRoleMapper;
import co.handk.backend.service.PermissionQueryService;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.PermissionTypeEnum;
import co.handk.common.enums.StatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionQueryServiceImpl implements PermissionQueryService {

    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public boolean isSuperAdmin(Long userId) {
        try {
            List<Role> roles = getRoles(userId);
            return roles.stream().anyMatch(role -> SecurityConstant.ROLE_SUPER_ADMIN.equals(role.getCode()));
        } catch (Exception ex) {
            log.error("isSuperAdmin query failed, userId={}", userId, ex);
            return false;
        }
    }

    @Override
    public Set<String> getPermissionCodes(Long userId) {
        try {
            boolean superAdmin = isSuperAdmin(userId);
            List<Long> roleIds = getRoleIds(userId);
            if (roleIds.isEmpty()) {
                if (superAdmin) {
                    Set<String> adminCodes = new HashSet<>();
                    adminCodes.add(SecurityConstant.ROLE_SUPER_ADMIN);
                    adminCodes.add(SecurityConstant.DATA_ALL_WRITE);
                    permissionMapper.selectList(
                                    new QueryWrapper<Permission>()
                                            .eq("deleted", DeleteEnum.UNDELETED.getCode())
                                            .eq("status", StatusEnum.NOMAL.getCode())
                            ).stream()
                            .map(Permission::getCode)
                            .filter(code -> code != null && !code.isBlank())
                            .forEach(adminCodes::add);
                    return adminCodes;
                }
                Set<String> normalCodes = new HashSet<>();
                normalCodes.add(SecurityConstant.ROLE_NORMAL_USER);
                return normalCodes;
            }
            List<Object> permissionIdObjs = rolePermissionMapper.selectObjs(
                    new QueryWrapper<RolePermission>()
                            .select("permission_id")
                            .in("role_id", roleIds)
                            .eq("deleted", DeleteEnum.UNDELETED.getCode())
            );
            List<Long> permissionIds = permissionIdObjs.stream()
                    .map(obj -> ((Number) obj).longValue())
                    .distinct()
                    .toList();
            QueryWrapper<Permission> permissionQuery = new QueryWrapper<Permission>()
                    .eq("deleted", DeleteEnum.UNDELETED.getCode())
                    .eq("status", StatusEnum.NOMAL.getCode());
            if (!superAdmin) {
                if (permissionIds.isEmpty()) {
                    return Collections.emptySet();
                }
                permissionQuery.in("id", permissionIds);
            }
            Set<String> codes = permissionMapper.selectList(permissionQuery).stream()
                    .map(Permission::getCode)
                    .filter(code -> code != null && !code.isBlank())
                    .collect(Collectors.toSet());
            if (superAdmin) {
                codes.add(SecurityConstant.ROLE_SUPER_ADMIN);
                codes.add(SecurityConstant.DATA_ALL_WRITE);
            } else {
                codes.add(SecurityConstant.ROLE_NORMAL_USER);
            }
            return codes;
        } catch (Exception ex) {
            log.error("getPermissionCodes query failed, userId={}", userId, ex);
            return Collections.emptySet();
        }
    }

    @Override
    public List<Permission> getEnabledDataPermissions() {
        try {
            return permissionMapper.selectList(
                    new QueryWrapper<Permission>()
                            .eq("deleted", DeleteEnum.UNDELETED.getCode())
                            .eq("status", StatusEnum.NOMAL.getCode())
                            .eq("type", PermissionTypeEnum.DATA.getCode())
            );
        } catch (Exception ex) {
            log.error("getEnabledDataPermissions query failed", ex);
            return Collections.emptyList();
        }
    }

    private List<Role> getRoles(Long userId) {
        List<Long> roleIds = getRoleIds(userId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return roleMapper.selectList(
                new QueryWrapper<Role>()
                        .in("id", roleIds)
                        .eq("deleted", DeleteEnum.UNDELETED.getCode())
                        .eq("status", StatusEnum.NOMAL.getCode())
        );
    }

    private List<Long> getRoleIds(Long userId) {
        if (userId == null || userId <= 0L) {
            return Collections.emptyList();
        }
        List<Object> roleIdObjs = userRoleMapper.selectObjs(
                new QueryWrapper<UserRole>()
                        .select("role_id")
                        .eq("user_id", userId)
                        .eq("deleted", DeleteEnum.UNDELETED.getCode())
        );
        return roleIdObjs.stream()
                .map(obj -> ((Number) obj).longValue())
                .distinct()
                .toList();
    }
}
