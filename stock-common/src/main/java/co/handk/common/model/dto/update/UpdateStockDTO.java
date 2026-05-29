package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.PositiveOrZero;

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
    @PositiveOrZero(message = "0以上で入力してください")
    private Integer currentQty;
    @PositiveOrZero(message = "0以上で入力してください")
    private Integer lockQty;
    @PositiveOrZero(message = "0以上で入力してください")
    private BigDecimal price;
    private String currency;
    private LocalDateTime priceUpdateTime;
    private Long stockTypeId;
    private StatusEnum status;

    @NotNull(message = "versionは必須です")
    private Long version;
}
