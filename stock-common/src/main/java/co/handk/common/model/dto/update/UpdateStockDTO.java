package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateStockDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

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

    private StatusEnum status;
    private Long version;
}

