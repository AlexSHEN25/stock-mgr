package co.handk.backend.service;

import co.handk.backend.entity.Permission;
import co.handk.common.model.vo.PermissionScopeVO;

import java.util.List;
import java.util.Set;

public interface PermissionQueryService {

    boolean isSuperAdmin(Long userId);

    Set<String> getPermissionCodes(Long userId);

    PermissionScopeVO getPermissionScope(Long userId);

    List<Permission> getEnabledDataPermissions();

    Set<String> getStockGroupCodes();
}
