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
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionQueryServiceImpl implements PermissionQueryService {

    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public boolean isSuperAdmin(Long userId) {
        List<Role> roles = getRoles(userId);
        return roles.stream().anyMatch(role -> SecurityConstant.ROLE_SUPER_ADMIN.equals(role.getCode()));
    }

    @Override
    public Set<String> getPermissionCodes(Long userId) {
        List<Long> roleIds = getRoleIds(userId);
        if (roleIds.isEmpty()) {
            return Collections.emptySet();
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
        if (permissionIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<Permission> permissions = permissionMapper.selectList(
                new QueryWrapper<Permission>()
                        .in("id", permissionIds)
                        .eq("deleted", DeleteEnum.UNDELETED.getCode())
                        .eq("status", StatusEnum.NOMAL.getCode())
        );
        return permissions.stream()
                .map(Permission::getCode)
                .filter(code -> code != null && !code.isBlank())
                .collect(Collectors.toSet());
    }

    @Override
    public List<Permission> getEnabledDataPermissions() {
        return permissionMapper.selectList(
                new QueryWrapper<Permission>()
                        .eq("deleted", DeleteEnum.UNDELETED.getCode())
                        .eq("status", StatusEnum.NOMAL.getCode())
                        .eq("type", PermissionTypeEnum.DATA.getCode())
        );
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
