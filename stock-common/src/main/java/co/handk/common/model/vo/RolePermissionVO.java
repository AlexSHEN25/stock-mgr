package co.handk.common.model.vo;

import lombok.Data;

@Data
public class RolePermissionVO extends BaseVO {
    private Long roleId;
    private Long permissionId;
}
