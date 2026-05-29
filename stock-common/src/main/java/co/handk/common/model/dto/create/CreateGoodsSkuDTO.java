package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import co.handk.common.enums.StatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateGoodsSkuDTO {

    @NotNull(message = "必須項目です")
    private Long goodsId;

    @NotBlank(message = "必須項目です")
    private String skuCode;

    private String skuName;

    @PositiveOrZero(message = "0以上で入力してください")
    private BigDecimal price;
    private String currency;

    @PositiveOrZero(message = "0以上で入力してください")
    private BigDecimal costPrice;

    @PositiveOrZero(message = "0以上で入力してください")
    private BigDecimal updatePrice;

    private LocalDateTime priceUpdateTime;

    private String barcode;

    private BigDecimal weight;

    private BigDecimal volume;

    private StatusEnum status;

}
