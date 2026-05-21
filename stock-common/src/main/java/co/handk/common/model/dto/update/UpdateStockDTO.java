package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateStockDTO {
    @NotNull(message = "IDは必須です")
    private Long id;

    @NotNull(message = "商品IDは必須です")
    private Integer goodsId;

    @NotBlank(message = "商品名は必須です")
    private String goodsName;

    @NotNull(message = "SKU IDは必須です")
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

    @NotNull(message = "versionは必須です")
    private Long version;
}
