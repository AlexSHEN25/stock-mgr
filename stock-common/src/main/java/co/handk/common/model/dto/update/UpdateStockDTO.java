package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UpdateStockDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private Integer goodsId;
    private String goodsName;
    private String sku;
    private Integer warehouseId;
    private Integer currentQty;
    private Integer lockQty;
    private BigDecimal price;
    private LocalDateTime priceUpdateTime;
    private StatusEnum status;
    private Long version;
}
