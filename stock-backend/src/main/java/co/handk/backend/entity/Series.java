package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

@Data
@Schema(resource = "series", name = "商品シリーズ", group = "システム管理/商品管理")
public class Series extends BaseEntity {

    @SchemaField(title = "系列名称")
    private String name;

    @SchemaField(title = "英文名")
    private String englishName;

    @SchemaField(title = "系列简介")
    private String content;

    @SchemaField(title = "状态:1启用0停用")
    private Integer status;
}