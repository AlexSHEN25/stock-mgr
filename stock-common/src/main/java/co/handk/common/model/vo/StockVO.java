package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockVO extends BaseVO {
    @SchemaField(label = "\u5546\u54c1ID", order = 50)
    private Integer goodsId;
    @SchemaField(label = "\u5546\u54c1\u540d", order = 50)
    private String goodsName;
    @SchemaField(label = "SKU ID", order = 50)
    private Long skuId;
    @SchemaField(label = "SKU\u30b3\u30fc\u30c9", order = 50)
    private String skuCode;

    @SchemaField(label = "\u5009\u5eabID", order = 50)
    private Integer warehouseId;
    @SchemaField(label = "\u73fe\u5728\u6570\u91cf", order = 50)
    private Integer currentQty;
    @SchemaField(label = "\u30ed\u30c3\u30af\u6570\u91cf", order = 50)
    private Integer lockQty;
    @SchemaField(label = "\u4fa1\u683c", order = 50)
    private BigDecimal price;
    @SchemaField(label = "\u901a\u8ca8", order = 50)
    private String currency;
    @SchemaField(label = "\u4fa1\u683c\u66f4\u65b0\u65e5\u6642", order = 50)
    private LocalDateTime priceUpdateTime;
    @SchemaField(label = "\u5728\u5eab\u533a\u5206ID", order = 50)
    private Integer stockTypeId;
    @SchemaField(label = "\u72b6\u614b\u30b3\u30fc\u30c9", order = 40)
    private Integer status;
    @SchemaField(label = "\u72b6\u614b", order = 41)
    private String statusDesc;
}
