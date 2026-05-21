package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import co.handk.common.enums.StatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateGoodsSkuDTO {

    @NotNull(message = "必須項目です")
    private Long goodsId;

    @NotBlank(message = "必須項目です")
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
