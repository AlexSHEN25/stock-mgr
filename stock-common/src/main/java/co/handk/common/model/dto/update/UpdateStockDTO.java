package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UpdateStockDTO {
    @NotNull(message = "ID荳崎・荳ｺ遨ｺ")
    private Long id;

    private Integer goodsId;
    private String goodsName;
    private Long skuId;
    private String sku;
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

