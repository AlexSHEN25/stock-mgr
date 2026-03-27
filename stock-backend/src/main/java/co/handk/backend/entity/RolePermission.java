package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RolePermission extends BaseEntity {

    private Long roleId;

    private Long permissionId;
}
