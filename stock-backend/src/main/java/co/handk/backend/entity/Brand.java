package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

@Data
@Schema(resource = "brand", name = "ブランド", group = "システム管理/商品管理")
public class Brand extends BaseEntity {

    @SchemaField(title = "品牌名称")
    private String name;

    @SchemaField(title = "英文名")
    private String englishName;

    @SchemaField(title = "品牌封面图")
    private String image;

    @SchemaField(title = "品牌简介")
    private String content;

    @SchemaField(title = "状态:1启用0停用")
    private Integer status;
}