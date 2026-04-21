package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

@Data
@Schema(resource = "permission", name = "権限", group = "システム管理/ユーザー管理")
public class Permission extends BaseEntity {

    @SchemaField(title = "权限名称")
    private String name;

    @SchemaField(title = "权限标识")
    private String code;

    @SchemaField(title = "所属模块")
    private String module;

    @SchemaField(title = "类型:1菜单2按钮3接口")
    private Integer type;

    @SchemaField(title = "父级权限")
    private Long parentId;

    @SchemaField(title = "前端路由")
    private String path;

    @SchemaField(title = "排序")
    private Integer sort;

    @SchemaField(title = "图标")
    private String icon;

    @SchemaField(title = "前端组件路径")
    private String component;

    @SchemaField(title = "状态")
    private Integer status;
}