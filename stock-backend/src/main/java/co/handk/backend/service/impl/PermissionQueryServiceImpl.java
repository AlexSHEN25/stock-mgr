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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionQueryServiceImpl implements PermissionQueryService {
    private static final String DATA_SCOPE_ALL = "ALL";
    private static final String DATA_SCOPE_OWN = "OWN";
    private static final String DATA_SCOPE_READONLY = "READONLY";
    private static final Set<String> NORMAL_USER_OWN_WRITE_MODULES = Set.of(
            "stockOrder",
            "stockOrderItem",
            "requestForm",
            "requestItem",
            "customer"
    );
    private static final Set<String> NORMAL_USER_ALL_WRITE_MODULES = Set.of("customerLevel");
    private static final Map<String, String> MENU_LABEL_BY_MODULE = Map.ofEntries(
            Map.entry("user", "\u30e6\u30fc\u30b6\u30fc\u7ba1\u7406"),
            Map.entry("dept", "\u90e8\u7f72\u7ba1\u7406"),
            Map.entry("warehouse", "\u5009\u5eab\u7ba1\u7406"),
            Map.entry("role", "\u30ed\u30fc\u30eb\u7ba1\u7406"),
            Map.entry("permission", "\u6a29\u9650\u7ba1\u7406"),
            Map.entry("goods", "\u5546\u54c1\u7ba1\u7406"),
            Map.entry("maker", "\u30e1\u30fc\u30ab\u30fc\u7ba1\u7406"),
            Map.entry("brand", "\u30d6\u30e9\u30f3\u30c9\u7ba1\u7406"),
            Map.entry("category", "\u30ab\u30c6\u30b4\u30ea\u7ba1\u7406"),
            Map.entry("series", "\u30b7\u30ea\u30fc\u30ba\u7ba1\u7406"),
            Map.entry("stock", "\u81ea\u793e\u5728\u5eab\u7ba1\u7406"),
            Map.entry("stockType", "\u5728\u5eab\u5206\u985e"),
            Map.entry("stockOrder", "\u5165\u51fa\u5eab\u4f1d\u7968"),
            Map.entry("stockOrderItem", "\u5165\u51fa\u5eab\u660e\u7d30"),
            Map.entry("stockRecord", "\u5728\u5eab\u5c65\u6b74"),
            Map.entry("priceRecord", "\u4fa1\u683c\u5c65\u6b74"),
            Map.entry("requestForm", "\u8acb\u6c42\u66f8\u7ba1\u7406"),
            Map.entry("requestItem", "\u8acb\u6c42\u66f8\u660e\u7d30"),
            Map.entry("customer", "\u9867\u5ba2\u7ba1\u7406"),
            Map.entry("customerLevel", "\u9867\u5ba2\u30e9\u30f3\u30af\u7ba1\u7406"),
            Map.entry("config", "\u30b7\u30b9\u30c6\u30e0\u8a2d\u5b9a"),
            Map.entry("message", "\u30e1\u30c3\u30bb\u30fc\u30b8\u7ba1\u7406"),
            Map.entry("operateLog", "\u64cd\u4f5c\u30ed\u30b0")
    );

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
                return getNormalUserPermissionCodes();
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
                    return getNormalUserPermissionCodes();
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
                codes = getNormalUserPermissionCodes();
            }
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
        List<Permission> dataPermissions = getEnabledDataPermissions().stream()
                .filter(permission -> permission.getPath() != null && !permission.getPath().isBlank())
                .sorted(Comparator
                        .comparing((Permission permission) -> permission.getSort() == null ? 0 : permission.getSort())
                        .thenComparing(permission -> permission.getCode() == null ? "" : permission.getCode()))
                .toList();

        for (Permission permission : dataPermissions) {
            String key = resolveModuleKey(permission.getPath());
            String code = permission.getCode();
            if (key.isBlank() || code == null || code.isBlank()) {
                continue;
            }
            PermissionScopeVO.MenuPermissionVO menu = menus.computeIfAbsent(key, ignored -> {
                PermissionScopeVO.MenuPermissionVO item = new PermissionScopeVO.MenuPermissionVO();
                item.setKey(key);
                item.setLabel(resolveMenuLabel(permission));
                item.setModule(permission.getModule());
                item.setPath(permission.getPath());
                item.setDataScope(resolveDataScope(key, allDataWrite, codes));
                item.setSort(permission.getSort());
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
                menu.setDataScope(resolveDataScope(key, allDataWrite, codes));
            }
        }
        scope.setMenus(menus.values().stream()
                .filter(menu -> menu.getActions().isRead())
                .sorted(Comparator
                        .comparing((PermissionScopeVO.MenuPermissionVO menu) -> menu.getSort() == null ? 0 : menu.getSort())
                        .thenComparing(PermissionScopeVO.MenuPermissionVO::getKey))
                .toList());
        return scope;
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

    private Set<String> getNormalUserPermissionCodes() {
        Set<String> codes = permissionMapper.selectList(
                        new QueryWrapper<Permission>()
                                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                                .eq("status", StatusEnum.NOMAL.getCode())
                ).stream()
                .filter(this::isNormalUserPermission)
                .map(Permission::getCode)
                .filter(code -> code != null && !code.isBlank())
                .collect(Collectors.toCollection(HashSet::new));
        codes.add(SecurityConstant.ROLE_NORMAL_USER);
        return codes;
    }

    private String resolveModuleKey(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        String value = path.trim();
        if (value.startsWith(SecurityConstant.API_PREFIX)) {
            value = value.substring(SecurityConstant.API_PREFIX.length());
        } else if (value.startsWith("/")) {
            value = value.substring(1);
        }
        int slash = value.indexOf('/');
        if (slash >= 0) {
            value = value.substring(0, slash);
        }
        return value.replace("*", "").trim();
    }

    private String resolveMenuLabel(Permission permission) {
        String moduleKey = resolveModuleKey(permission.getPath());
        String fixedLabel = MENU_LABEL_BY_MODULE.get(moduleKey);
        if (fixedLabel != null && !fixedLabel.isBlank()) {
            return fixedLabel;
        }
        String name = permission.getName();
        if (name == null || name.isBlank()) {
            return moduleKey;
        }
        return name
                .replace("\u95b2\u89a7", "")
                .replace("\u7de8\u96c6", "")
                .replace("\u53c2\u7167", "")
                .replace("\u66f4\u65b0", "")
                .replace("\u8aad\u53d6", "")
                .replace("\u66f8\u8fbc", "")
                .replaceAll("[-\u30fb\\s]+$", "")
                .trim();
    }

    private String resolveDataScope(String moduleKey, boolean allDataWrite, Set<String> codes) {
        if (allDataWrite) {
            return DATA_SCOPE_ALL;
        }
        if (NORMAL_USER_OWN_WRITE_MODULES.contains(moduleKey) && hasWriteCode(moduleKey, codes)) {
            return DATA_SCOPE_OWN;
        }
        if (NORMAL_USER_ALL_WRITE_MODULES.contains(moduleKey) && hasWriteCode(moduleKey, codes)) {
            return DATA_SCOPE_ALL;
        }
        return DATA_SCOPE_READONLY;
    }

    private boolean hasWriteCode(String moduleKey, Set<String> codes) {
        String expected = "DATA_" + moduleKey.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase()
                + SecurityConstant.PERMISSION_SUFFIX_WRITE;
        return codes != null && codes.contains(expected);
    }

    private boolean isNormalUserPermission(Permission permission) {
        if (permission == null) {
            return false;
        }
        Integer type = permission.getType();
        if (!PermissionTypeEnum.DATA.getCode().equals(type)) {
            return true;
        }
        String code = permission.getCode();
        if (code == null || code.isBlank()) {
            return false;
        }
        if (code.endsWith(SecurityConstant.PERMISSION_SUFFIX_READ)) {
            return true;
        }
        return code.endsWith(SecurityConstant.PERMISSION_SUFFIX_WRITE)
                && SecurityConstant.isNormalUserWriteApiPath(permission.getPath());
    }
}

