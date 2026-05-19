package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateGoodsSkuDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

    @NotNull(message = "商品は必須項目です")
    private Long goodsId;

    @NotBlank(message = "SKUコードは必須項目です")
    private String skuCode;

    private String skuName;

    private BigDecimal price;

    private String currency;

    private BigDecimal costPrice;

    private BigDecimal updatePrice;

    private LocalDateTime priceUpdateTime;

    private String barcode;

    private BigDecimal weight;

    private BigDecimal volume;

    private StatusEnum status;
}

