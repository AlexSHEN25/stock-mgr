package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsSku extends BaseEntity {

    private Long goodsId;

    private String skuCode;

    private String skuName;

    private BigDecimal price;

    private String currency;

    private BigDecimal costPrice;

    private BigDecimal updatePrice;

    private LocalDateTime priceUpdateTime;

    private String barcode;

    private BigDecimal weight;

    private BigDecimal volume;

    private Integer status;
}

