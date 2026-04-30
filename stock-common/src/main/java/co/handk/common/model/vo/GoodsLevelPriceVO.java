package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsLevelPriceVO extends BaseVO {
    @SchemaField(label = "\u5546\u54c1ID", order = 50)
    private Long goodsId;
    @SchemaField(label = "SKU ID", order = 50)
    private Long skuId;
    @SchemaField(label = "SKU\u30b3\u30fc\u30c9", order = 50)
    private String skuCode;
    @SchemaField(label = "\u30e9\u30f3\u30afID", order = 50)
    private Long levelId;
    @SchemaField(label = "\u4fa1\u683c", order = 50)
    private BigDecimal price;
    @SchemaField(label = "\u901a\u8ca8", order = 50)
    private String currency;
    @SchemaField(label = "\u5272\u5f15\u7387", order = 50)
    private BigDecimal discount;
    @SchemaField(label = "\u6709\u52b9\u958b\u59cb\u65e5\u6642", order = 50)
    private LocalDateTime effectiveTime;
    @SchemaField(label = "\u6709\u52b9\u671f\u9650", order = 50)
    private LocalDateTime expireTime;
    @SchemaField(label = "\u72b6\u614b\u30b3\u30fc\u30c9", order = 40)
    private Integer status;
    @SchemaField(label = "\u72b6\u614b", order = 41)
    private String statusDesc;
}
