package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateGoodsSkuSpecDTO {

    @NotNull(message = "SKUは必須項目です")
    private Long skuId;

    private String skuCode;

    @NotNull(message = "仕様IDは必須項目です")
    private Long specId;

    @NotBlank(message = "仕様名は必須項目です")
    private String specName;

    @NotBlank(message = "仕様値は必須項目です")
    private String specValue;

    private Integer sort;
}
