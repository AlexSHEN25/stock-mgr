package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotBlank;
import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateStockDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

    @NotNull(message = "商品は必須項目です")
    private Integer goodsId;
    @NotBlank(message = "商品名は必須項目です")
    private String goodsName;
    @NotNull(message = "SKUは必須項目です")
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
}

