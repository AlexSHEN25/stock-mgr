package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsLevelPrice extends BaseEntity {

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

