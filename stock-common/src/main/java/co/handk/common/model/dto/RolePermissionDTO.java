package co.handk.common.model.dto;

import lombok.Data;

@Data
public class RolePermissionDTO {

    private Long id;

    private Long roleId;
    private Long permissionId;
}
