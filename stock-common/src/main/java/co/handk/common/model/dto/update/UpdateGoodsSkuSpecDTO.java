package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class UpdateGoodsSkuSpecDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

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
