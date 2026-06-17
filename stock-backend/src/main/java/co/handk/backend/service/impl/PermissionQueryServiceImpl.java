package co.handk.backend.service.impl;

import co.handk.backend.constant.SecurityConstant;
import co.handk.backend.entity.Dept;
import co.handk.backend.entity.Permission;
import co.handk.backend.entity.Role;
import co.handk.backend.entity.RolePermission;
import co.handk.backend.entity.User;
import co.handk.backend.entity.UserRole;
import co.handk.backend.mapper.DeptMapper;
import co.handk.backend.mapper.PermissionMapper;
import co.handk.backend.mapper.RoleMapper;
import co.handk.backend.mapper.RolePermissionMapper;
import co.handk.backend.mapper.UserMapper;
import co.handk.backend.mapper.UserRoleMapper;
import co.handk.backend.service.PermissionQueryService;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.PermissionTypeEnum;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.vo.PermissionScopeVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionQueryServiceImpl implements PermissionQueryService {
    private static final String DATA_SCOPE_ALL = "ALL";
    private static final String DATA_SCOPE_OWN = "OWN";
    private static final String DATA_SCOPE_GROUP = "GROUP";
    private static final String DATA_SCOPE_READONLY = "READONLY";
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final UserMapper userMapper;
    private final DeptMapper deptMapper;

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
            List<Role> roles = getRoles(userId);
            List<Long> roleIds = roles.stream().map(Role::getId).toList();
            Set<String> codes = roles.stream()
                    .map(Role::getCode)
                    .filter(code -> code != null && !code.isBlank())
                    .collect(Collectors.toCollection(HashSet::new));
            if (roleIds.isEmpty()) {
                return codes;
            }
            List<Object> permissionIdObjs = rolePermissionMapper.selectObjs(
                    new QueryWrapper<RolePermission>()
                            .select("permission_id")
                            .in("role_id", roleIds)
            );
            List<Long> permissionIds = permissionIdObjs.stream()
                    .map(obj -> ((Number) obj).longValue())
                    .distinct()
                    .toList();
            if (permissionIds.isEmpty()) {
                return codes;
            }
            permissionMapper.selectList(new QueryWrapper<Permission>()
                            .in("id", permissionIds)
                            .eq("status", StatusEnum.NOMAL.getCode()))
                    .stream()
                    .map(Permission::getCode)
                    .filter(code -> code != null && !code.isBlank())
                    .forEach(codes::add);
            log.debug("permission codes resolved, userId={}, codes={}", userId, codes);
            return codes;
        } catch (Exception ex) {
            log.error("getPermissionCodes query failed, userId={}", userId, ex);
            return Collections.emptySet();
        }
    }

    @Override
    public PermissionScopeVO getPermissionScope(Long userId) {
        Set<String> codes = getPermissionCodes(userId);
        boolean superAdmin = codes.contains(SecurityConstant.ROLE_SUPER_ADMIN);
        boolean allDataWrite = superAdmin || codes.contains(SecurityConstant.DATA_ALL_WRITE);
        String deptCode = resolveDeptCode(userId);
        PermissionScopeVO scope = new PermissionScopeVO();
        scope.setSuperAdmin(superAdmin);
        scope.setAllDataWrite(allDataWrite);
        scope.setMenuCodes(codes.stream()
                .filter(code -> code != null && code.startsWith("MENU_"))
                .collect(Collectors.toCollection(HashSet::new)));
        scope.setRoleCodes(codes.stream()
                .filter(code -> code != null && code.startsWith("ROLE_"))
                .collect(Collectors.toCollection(HashSet::new)));
        scope.setPermissionCodes(codes.stream()
                .filter(code -> code != null && !code.startsWith("MENU_") && !code.startsWith("ROLE_"))
                .collect(Collectors.toCollection(HashSet::new)));

        Map<String, PermissionScopeVO.MenuPermissionVO> menus = new LinkedHashMap<>();
        Map<Long, PermissionScopeVO.MenuPermissionVO> treeNodes = new LinkedHashMap<>();
        Map<Long, Permission> menuPermissionsById = getEnabledMenuPermissions().stream()
                .filter(permission -> permission.getId() != null)
                .collect(Collectors.toMap(Permission::getId, permission -> permission));
        List<Permission> dataPermissions = getEnabledDataPermissions().stream()
                .filter(permission -> permission.getPath() != null && !permission.getPath().isBlank())
                .sorted(Comparator
                        .comparing((Permission permission) -> permission.getSort() == null ? Integer.MAX_VALUE : permission.getSort())
                        .thenComparing(Permission::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Permission::getId, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(permission -> permission.getCode() == null ? "" : permission.getCode()))
                .toList();

        for (Permission permission : dataPermissions) {
            Permission parentMenu = menuPermissionsById.get(permission.getParentId());
            if (parentMenu == null || parentMenu.getCode() == null || !codes.contains(parentMenu.getCode())) {
                continue;
            }
            String key = resolveMenuKey(parentMenu.getPath());
            String code = permission.getCode();
            if (key.isBlank() || code == null || code.isBlank()) {
                continue;
            }
            PermissionScopeVO.MenuPermissionVO menu = menus.computeIfAbsent(key, ignored -> {
                PermissionScopeVO.MenuPermissionVO item = new PermissionScopeVO.MenuPermissionVO();
                item.setKey(key);
                item.setLabel(parentMenu.getName());
                item.setModule(parentMenu.getModule());
                item.setPath(parentMenu.getPath());
                item.setDataScope(DATA_SCOPE_READONLY);
                item.setSort(parentMenu.getSort());
                item.setId(parentMenu.getId());
                item.setParentId(parentMenu.getParentId());
                return item;
            });
            if (menu.getSort() == null || (permission.getSort() != null && permission.getSort() < menu.getSort())) {
                menu.setSort(permission.getSort());
            }
            PermissionScopeVO.ActionPermissionVO actions = menu.getActions();
            if (code.endsWith(SecurityConstant.PERMISSION_SUFFIX_READ) && (allDataWrite || codes.contains(code))) {
                actions.setRead(true);
            }
            if (code.endsWith(SecurityConstant.PERMISSION_SUFFIX_WRITE) && (allDataWrite || codes.contains(code))) {
                actions.setRead(true);
                actions.setCreate(true);
                actions.setEdit(true);
                actions.setDelete(true);
                actions.setBatchDelete(true);
                actions.setInlineEdit(true);
            }
            menu.setDataScope(resolveDataScope(parentMenu.getPath(), deptCode, allDataWrite, actions.isEdit()));
            menu.setVisible(actions.isRead());

            if (parentMenu.getId() != null) {
                PermissionScopeVO.MenuPermissionVO treeNode = treeNodes.computeIfAbsent(parentMenu.getId(), ignored -> {
                    PermissionScopeVO.MenuPermissionVO node = new PermissionScopeVO.MenuPermissionVO();
                    node.setId(parentMenu.getId());
                    node.setParentId(parentMenu.getParentId());
                    node.setKey(key);
                    node.setLabel(parentMenu.getName());
                    node.setModule(parentMenu.getModule());
                    node.setPath(parentMenu.getPath());
                    node.setDataScope(DATA_SCOPE_READONLY);
                    node.setSort(parentMenu.getSort());
                    return node;
                });
                treeNode.setVisible(menu.isVisible());
                treeNode.setDataScope(menu.getDataScope());
                treeNode.getActions().setRead(actions.isRead());
                treeNode.getActions().setCreate(actions.isCreate());
                treeNode.getActions().setEdit(actions.isEdit());
                treeNode.getActions().setDelete(actions.isDelete());
                treeNode.getActions().setBatchDelete(actions.isBatchDelete());
                treeNode.getActions().setInlineEdit(actions.isInlineEdit());
            }
        }
        scope.setMenus(menus.values().stream()
                .filter(menu -> menu.getActions().isRead())
                .toList());
        List<PermissionScopeVO.MenuPermissionVO> roots = new ArrayList<>();
        for (PermissionScopeVO.MenuPermissionVO node : treeNodes.values()) {
            Long parentId = node.getParentId();
            if (parentId != null && parentId > 0 && treeNodes.containsKey(parentId)) {
                treeNodes.get(parentId).getChildren().add(node);
            } else {
                roots.add(node);
            }
        }
        scope.setPermissionTree(roots.stream()
                .sorted(Comparator
                        .comparing((PermissionScopeVO.MenuPermissionVO menu) -> menu.getSort() == null ? Integer.MAX_VALUE : menu.getSort())
                        .thenComparing(menu -> menu.getId() == null ? Long.MAX_VALUE : menu.getId()))
                .toList());
        return scope;
    }

    @Override
    public List<Permission> getEnabledDataPermissions() {
        try {
            return permissionMapper.selectList(
                    new QueryWrapper<Permission>()
                            .eq("status", StatusEnum.NOMAL.getCode())
                            .eq("type", PermissionTypeEnum.DATA.getCode())
            );
        } catch (Exception ex) {
            log.error("getEnabledDataPermissions query failed", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public Set<String> getStockGroupCodes() {
        return getEnabledMenuPermissions().stream()
                .map(Permission::getPath)
                .filter(path -> path != null && path.matches("(?i)^/stock/group/[^/]+/?$"))
                .map(path -> path.replaceAll("/+$", ""))
                .map(path -> path.substring(path.lastIndexOf('/') + 1).trim().toUpperCase())
                .filter(code -> !code.isBlank())
                .collect(Collectors.toSet());
    }

    private List<Permission> getEnabledMenuPermissions() {
        try {
            return permissionMapper.selectList(
                    new QueryWrapper<Permission>()
                            .eq("status", StatusEnum.NOMAL.getCode())
                            .ne("type", PermissionTypeEnum.DATA.getCode())
            );
        } catch (Exception ex) {
            log.error("getEnabledMenuPermissions query failed", ex);
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
        );
        return roleIdObjs.stream()
                .map(obj -> ((Number) obj).longValue())
                .distinct()
                .toList();
    }

    private String resolveMenuKey(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        String[] segments = java.util.Arrays.stream(path.trim().split("/"))
                .filter(segment -> !segment.isBlank())
                .toArray(String[]::new);
        if (segments.length == 0) {
            return "";
        }
        StringBuilder key = new StringBuilder(segments[0]);
        int start = segments.length > 2 && "group".equalsIgnoreCase(segments[1]) ? 2 : 1;
        for (int index = start; index < segments.length; index++) {
            String segment = segments[index].replace("*", "").trim();
            if (!segment.isBlank()) {
                key.append(Character.toUpperCase(segment.charAt(0))).append(segment.substring(1));
            }
        }
        if (segments.length > 2 && "group".equalsIgnoreCase(segments[1])) {
            key.insert(segments[0].length(), "Group");
        }
        return key.toString();
    }

    private String resolveDataScope(String menuPath, String deptCode, boolean allDataWrite, boolean writable) {
        if (allDataWrite) {
            return DATA_SCOPE_ALL;
        }
        if (!writable) {
            return DATA_SCOPE_READONLY;
        }
        String normalizedPath = menuPath == null ? "" : menuPath.trim().replaceAll("/+$", "");
        if ("/stock/self".equalsIgnoreCase(normalizedPath)) {
            return DATA_SCOPE_OWN;
        }
        String groupPrefix = "/stock/group/";
        if (normalizedPath.regionMatches(true, 0, groupPrefix, 0, groupPrefix.length())
                && deptCode != null
                && normalizedPath.substring(groupPrefix.length()).equalsIgnoreCase(deptCode.trim())) {
            return DATA_SCOPE_GROUP;
        }
        return DATA_SCOPE_OWN;
    }

    private String resolveDeptCode(Long userId) {
        if (userId == null || userId <= 0L) {
            return null;
        }
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeptId() == null) {
            return null;
        }
        Dept dept = deptMapper.selectById(user.getDeptId());
        return dept == null ? null : dept.getCode();
    }

}
