package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsLevelPriceVO extends BaseVO {
    private Long goodsId;
    private Long skuId;
    private String skuCode;
    private Long levelId;
    private BigDecimal price;
    private String currency;
    private BigDecimal discount;
    private LocalDateTime effectiveTime;
    private LocalDateTime expireTime;
    private Integer status;
}

