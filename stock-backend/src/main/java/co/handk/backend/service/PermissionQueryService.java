package co.handk.backend.service;

import co.handk.backend.entity.Permission;

import java.util.List;
import java.util.Set;

public interface PermissionQueryService {

    boolean isSuperAdmin(Long userId);

    Set<String> getPermissionCodes(Long userId);

    List<Permission> getEnabledDataPermissions();
}
