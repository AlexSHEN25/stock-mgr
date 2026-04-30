package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PriceRecordVO extends BaseVO {
    @SchemaField(label = "\u5546\u54c1ID", order = 50)
    private Long goodsId;
    @SchemaField(label = "\u5546\u54c1\u540d", order = 50)
    private String goodsName;
    @SchemaField(label = "\u82f1\u8a9e\u540d", order = 50)
    private String englishName;
    @SchemaField(label = "SKU ID", order = 50)
    private Long skuId;
    @SchemaField(label = "SKU\u30b3\u30fc\u30c9", order = 50)
    private String skuCode;
    @SchemaField(label = "\u65e7\u4fa1\u683c", order = 50)
    private BigDecimal oldPrice;
    @SchemaField(label = "\u65b0\u4fa1\u683c", order = 50)
    private BigDecimal newPrice;
    @SchemaField(label = "\u901a\u8ca8", order = 50)
    private String currency;
    @SchemaField(label = "\u5272\u5f15\u7387", order = 50)
    private BigDecimal discount;
    @SchemaField(label = "\u4fa1\u683c\u66f4\u65b0\u65e5\u6642", order = 50)
    private LocalDateTime priceUpdateTime;
    @SchemaField(label = "\u64cd\u4f5c\u30e6\u30fc\u30b6\u30fcID", order = 50)
    private Long operatorId;
    @SchemaField(label = "\u64cd\u4f5c\u30e6\u30fc\u30b6\u30fc\u540d", order = 50)
    private String operatorName;
}
