package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CreateStockDTO {

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
}

