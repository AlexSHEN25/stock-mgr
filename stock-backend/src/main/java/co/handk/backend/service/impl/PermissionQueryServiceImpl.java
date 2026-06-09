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
import co.handk.backend.service.ConfigService;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.PermissionTypeEnum;
import co.handk.common.enums.StatusEnum;
import co.handk.backend.entity.Config;
import co.handk.common.model.vo.PermissionScopeVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
    private static final String DATA_SCOPE_READONLY = "READONLY";
    private static final String GROUP_CODE_CONFIG = "stock.group.codes";
    private static final String GROUP_MENU_JSON_CONFIG = "perm.group.menu.json";
    private static final Map<String, String> MENU_KEY_ALIASES = Map.of(
            "stockOrderItem", "stockOrder"
    );
    private static final Set<String> NORMAL_USER_OWN_WRITE_MODULES = Set.of(
            "stockOrder",
            "stockOrderItem",
            "requestForm",
            "requestItem",
            "customer"
    );
    private static final Set<String> NORMAL_USER_ALL_WRITE_MODULES = Set.of("customerLevel");
    private static final Map<String, String> MENU_LABEL_BY_MODULE = Map.ofEntries(
            Map.entry("user", "ユーザー管理"),
            Map.entry("dept", "部署管理"),
            Map.entry("warehouse", "倉庫管理"),
            Map.entry("role", "ロール管理"),
            Map.entry("permission", "権限管理"),
            Map.entry("goods", "商品管理"),
            Map.entry("maker", "メーカー管理"),
            Map.entry("brand", "ブランド管理"),
            Map.entry("category", "カテゴリ管理"),
            Map.entry("series", "シリーズ管理"),
            Map.entry("stock", "在庫管理"),
            Map.entry("stockType", "在庫区分"),
            Map.entry("stockOrder", "入出庫伝票"),
            Map.entry("stockOrderItem", "入出庫明細"),
            Map.entry("stockRecord", "在庫履歴"),
            Map.entry("priceRecord", "価格履歴"),
            Map.entry("requestForm", "請求書管理"),
            Map.entry("requestItem", "請求書明細"),
            Map.entry("customer", "顧客管理"),
            Map.entry("customerLevel", "顧客ランク管理"),
            Map.entry("config", "システム設定"),
            Map.entry("message", "メッセージ管理"),
            Map.entry("operateLog", "操作ログ")
    );

    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final UserMapper userMapper;
    private final DeptMapper deptMapper;
    private final ConfigService configService;
    private final ObjectMapper objectMapper;

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
        List<Permission> dataPermissions = getEnabledDataPermissions().stream()
                .filter(permission -> permission.getPath() != null && !permission.getPath().isBlank())
                .sorted(Comparator
                        .comparing((Permission permission) -> permission.getSort() == null ? Integer.MAX_VALUE : permission.getSort())
                        .thenComparing(Permission::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Permission::getId, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(permission -> permission.getCode() == null ? "" : permission.getCode()))
                .toList();

        for (Permission permission : dataPermissions) {
            String key = resolveMenuKey(permission.getPath());
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
                .filter(menu -> isVisibleMenuForUser(menu.getKey(), deptCode, superAdmin))
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

    private String resolveMenuKey(String path) {
        String key = resolveModuleKey(path);
        return MENU_KEY_ALIASES.getOrDefault(key, key);
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
                .replace("閲覧", "")
                .replace("編集", "")
                .replace("参照", "")
                .replace("更新", "")
                .replace("読取", "")
                .replace("書込", "")
                .replaceAll("[-・\\s]+$", "")
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

    private boolean isVisibleMenuForUser(String moduleKey, String deptCode, boolean superAdmin) {
        if (superAdmin) {
            return true;
        }
        if (deptCode == null || deptCode.isBlank()) {
            return true;
        }
        String normalizedDeptCode = deptCode.trim().toUpperCase();
        if (!isConfiguredGroupDept(normalizedDeptCode)) {
            return true;
        }
        Set<String> configuredMenuCodes = getGroupMenuCodes(normalizedDeptCode);
        if (configuredMenuCodes.isEmpty()) {
            log.warn("group menu config missing or empty, deptCode={}, allow menu by default", normalizedDeptCode);
            return true;
        }
        return configuredMenuCodes.contains(moduleKey);
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

    private boolean isConfiguredGroupDept(String deptCode) {
        return getConfiguredGroupDeptCodes().contains(deptCode);
    }

    private Set<String> getConfiguredGroupDeptCodes() {
        return parseCsvConfig(GROUP_CODE_CONFIG);
    }

    private Set<String> getGroupMenuCodes(String deptCode) {
        Map<String, Set<String>> configured = parseGroupMenuJsonConfig();
        Set<String> codes = configured.get(deptCode.toUpperCase());
        return codes == null ? Collections.emptySet() : codes;
    }

    private Map<String, Set<String>> parseGroupMenuJsonConfig() {
        Config config = configService.getOne(new QueryWrapper<Config>()
                .select("value")
                .eq("name", GROUP_MENU_JSON_CONFIG)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
        if (config == null || config.getValue() == null || config.getValue().isBlank()) {
            return Collections.emptyMap();
        }
        try {
            Map<String, List<String>> raw = objectMapper.readValue(
                    config.getValue(), new TypeReference<Map<String, List<String>>>() {
                    });
            Map<String, Set<String>> result = new LinkedHashMap<>();
            if (raw != null) {
                for (Map.Entry<String, List<String>> entry : raw.entrySet()) {
                    if (entry.getKey() == null || entry.getValue() == null) {
                        continue;
                    }
                    Set<String> menuCodes = entry.getValue().stream()
                            .filter(value -> value != null && !value.isBlank())
                            .map(String::trim)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    if (!menuCodes.isEmpty()) {
                        result.put(entry.getKey().trim().toUpperCase(), menuCodes);
                    }
                }
            }
            return result;
        } catch (Exception ex) {
            log.warn("parse group menu json config failed, configName={}", GROUP_MENU_JSON_CONFIG, ex);
            return Collections.emptyMap();
        }
    }

    private Set<String> parseCsvConfig(String name) {
        if (name == null || name.isBlank()) {
            return Collections.emptySet();
        }
        Config config = configService.getOne(new QueryWrapper<Config>()
                .select("value")
                .eq("name", name)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
        if (config == null || config.getValue() == null || config.getValue().isBlank()) {
            return Collections.emptySet();
        }
        return java.util.Arrays.stream(config.getValue().split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(HashSet::new));
    }
}


