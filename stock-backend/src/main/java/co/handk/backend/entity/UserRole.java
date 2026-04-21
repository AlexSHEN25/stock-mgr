package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

@Data
@Schema(resource = "userRole", name = "ユーザーロール関連", group = "システム管理/ユーザー管理")
public class UserRole extends BaseEntity {

    @SchemaField(title = "用户ID")
    private Long userId;

    @SchemaField(title = "角色ID")
    private Long roleId;
}