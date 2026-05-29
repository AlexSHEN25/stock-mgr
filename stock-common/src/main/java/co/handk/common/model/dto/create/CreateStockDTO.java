package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import co.handk.common.enums.StatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateStockDTO {

    @NotNull(message = "必須項目です")
    private Integer goodsId;
    @NotBlank(message = "必須項目です")
    private String goodsName;
    @NotNull(message = "必須項目です")
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

}
