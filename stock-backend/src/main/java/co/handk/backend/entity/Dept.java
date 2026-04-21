package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import co.handk.schema.enums.FieldType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(resource = "dept", name = "部署", group = "システム管理/ユーザー管理")
public class Dept extends BaseEntity {

    @SchemaField(title = "父部门ID")
    private Long parentId;

    @SchemaField(title = "部门名称")
    private String name;

    @SchemaField(title = "部门编码")
    private String code;

    @SchemaField(title = "部门负责人ID")
    private Long leaderId;

    @SchemaField(title = "排序")
    private Integer sort;

    @SchemaField(title = "状态:1正常0停用", type = FieldType.SWITCH)
    private Integer status;
}