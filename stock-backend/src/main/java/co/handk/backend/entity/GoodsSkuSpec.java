package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

@Data
@Schema(resource = "goodsSkuSpec", name = "SKU仕様属性", group = "システム管理/商品管理")
public class GoodsSkuSpec extends BaseEntity {

    @SchemaField(title = "SKU ID")
    private Long skuId;

    @SchemaField(title = "商品品番")
    private String skuCode;

    @SchemaField(title = "规格ID")
    private Long specId;

    @SchemaField(title = "规格名称")
    private String specName;

    @SchemaField(title = "规格值")
    private String specValue;

    @SchemaField(title = "sort")
    private Integer sort;
}