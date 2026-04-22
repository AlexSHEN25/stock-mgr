package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class Stock extends BaseEntity {

    private Integer goodsId;

    private String goodsName;

    private Long skuId;

    private String skuCode;

    private Integer warehouseId;

    private Integer currentQty;

    private Integer lockQty;

    private BigDecimal price;

    private String currency;

    private LocalDateTime priceUpdateTime;

    private Long stockTypeId;

    private Integer status;

    private Long version;
}

