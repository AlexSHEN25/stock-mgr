package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreatePriceRecordDTO {

    @NotNull(message = "商品は必須項目です")
    private Long goodsId;
    @NotBlank(message = "商品名は必須項目です")
    private String goodsName;
    private String englishName;
    @NotNull(message = "SKUは必須項目です")
    private Long skuId;
    private String skuCode;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private String currency;
    private BigDecimal discount;
    private LocalDateTime priceUpdateTime;
    @NotNull(message = "操作者IDは必須項目です")
    private Long operatorId;
    @NotBlank(message = "操作者名は必須項目です")
    private String operatorName;
}

