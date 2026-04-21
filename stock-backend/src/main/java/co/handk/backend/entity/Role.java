package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

@Data
@Schema(resource = "role", name = "ロール権限", group = "システム管理/ユーザー管理")
public class Role extends BaseEntity {

    @SchemaField(title = "角色名称")
    private String name;

    @SchemaField(title = "角色编码")
    private String code;

    @SchemaField(title = "备注")
    private String remark;

    @SchemaField(title = "状态")
    private Integer status;
}