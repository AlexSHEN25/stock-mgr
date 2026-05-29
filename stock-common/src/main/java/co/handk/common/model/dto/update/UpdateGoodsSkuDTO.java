package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class UpdateGoodsSkuDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

    @NotNull(message = "商品は必須項目です")
    private Long goodsId;

    @NotBlank(message = "SKUコードは必須項目です")
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

