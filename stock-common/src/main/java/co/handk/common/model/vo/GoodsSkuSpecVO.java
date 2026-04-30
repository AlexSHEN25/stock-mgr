package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

@Data
public class GoodsSkuSpecVO extends BaseVO {
    @SchemaField(label = "SKU ID", order = 50)
    private Long skuId;
    @SchemaField(label = "SKU\u30b3\u30fc\u30c9", order = 50)
    private String skuCode;
    @SchemaField(label = "\u4ed5\u69d8ID", order = 50)
    private Long specId;
    @SchemaField(label = "\u4ed5\u69d8\u540d", order = 50)
    private String specName;
    @SchemaField(label = "\u4ed5\u69d8\u5024", order = 50)
    private String specValue;
    @SchemaField(label = "\u4e26\u3073\u9806", order = 50)
    private Integer sort;
}
