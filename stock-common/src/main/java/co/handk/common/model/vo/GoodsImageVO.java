package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

@Data
public class GoodsImageVO extends BaseVO {
    @SchemaField(label = "\u5546\u54c1ID", order = 50)
    private Long goodsId;
    @SchemaField(label = "SKU ID", order = 50)
    private Long skuId;
    @SchemaField(label = "SKU\u30b3\u30fc\u30c9", order = 50)
    private String skuCode;
    @SchemaField(label = "\u753b\u50cfURL", order = 50)
    private String imageUrl;
    @SchemaField(label = "\u4e26\u3073\u9806", order = 50)
    private Integer sort;
}
