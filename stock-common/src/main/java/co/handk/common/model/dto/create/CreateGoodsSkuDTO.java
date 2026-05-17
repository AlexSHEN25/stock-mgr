package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateGoodsSkuDTO {

    private Long goodsId;

    @NotBlank(message = "SKUコードは必須です")
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

}
