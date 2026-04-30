package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsSkuVO extends BaseVO {
    @SchemaField(label = "\u5546\u54c1ID", order = 50)
    private Long goodsId;
    @SchemaField(label = "SKU\u30b3\u30fc\u30c9", order = 50)
    private String skuCode;
    @SchemaField(label = "SKU\u540d", order = 50)
    private String skuName;
    @SchemaField(label = "\u4fa1\u683c", order = 50)
    private BigDecimal price;
    @SchemaField(label = "\u901a\u8ca8", order = 50)
    private String currency;
    @SchemaField(label = "\u539f\u4fa1", order = 50)
    private BigDecimal costPrice;
    @SchemaField(label = "\u66f4\u65b0\u4fa1\u683c", order = 50)
    private BigDecimal updatePrice;
    @SchemaField(label = "\u4fa1\u683c\u66f4\u65b0\u65e5\u6642", order = 50)
    private LocalDateTime priceUpdateTime;
    @SchemaField(label = "\u30d0\u30fc\u30b3\u30fc\u30c9", order = 50)
    private String barcode;
    @SchemaField(label = "\u91cd\u91cf", order = 50)
    private BigDecimal weight;
    @SchemaField(label = "\u5bb9\u91cf", order = 50)
    private BigDecimal volume;
    @SchemaField(label = "\u72b6\u614b\u30b3\u30fc\u30c9", order = 40)
    private Integer status;
    @SchemaField(label = "\u72b6\u614b", order = 41)
    private String statusDesc;
}
