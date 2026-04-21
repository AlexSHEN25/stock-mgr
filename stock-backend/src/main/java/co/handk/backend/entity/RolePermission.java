package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

@Data
@Schema(resource = "rolePermission", name = "ロール権限関連", group = "システム管理/ユーザー管理")
public class RolePermission extends BaseEntity {

    @SchemaField(title = "角色ID")
    private Long roleId;

    @SchemaField(title = "权限ID")
    private Long permissionId;
}