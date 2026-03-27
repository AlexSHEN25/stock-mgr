package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class PriceRecord extends BaseEntity {

    private Long goodsId;

    private String goodsName;

    private String englishName;

    private String sku;

    private BigDecimal oldPrice;

    private BigDecimal newPrice;

    private BigDecimal discount;

    private LocalDateTime priceUpdateTime;

    private Long operatorId;

    private String operatorName;
}
