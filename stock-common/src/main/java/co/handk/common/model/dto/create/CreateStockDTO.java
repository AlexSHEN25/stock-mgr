package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateStockDTO {

    private Integer goodsId;
    private String goodsName;
    private Long skuId;
    private String skuCode;
    private Integer typeId;
    private Integer warehouseId;
    private Integer currentQty;
    private Integer lockQty;
    private BigDecimal price;
    private String currency;
    private LocalDateTime priceUpdateTime;
    private StatusEnum status;
    private Long version;
}

