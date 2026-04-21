package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

@Data
@Schema(resource = "goodsImage", name = "商品画像", group = "システム管理/商品管理")
public class GoodsImage extends BaseEntity {

    @SchemaField(title = "商品ID")
    private Long goodsId;

    @SchemaField(title = "SKU ID")
    private Long skuId;

    @SchemaField(title = "商品品番")
    private String skuCode;

    @SchemaField(title = "图片地址")
    private String imageUrl;

    @SchemaField(title = "排序")
    private Integer sort;
}