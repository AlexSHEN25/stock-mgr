package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateGoodsImageDTO {

    @NotNull(message = "商品は必須項目です")
    private Long goodsId;

    @NotNull(message = "SKUは必須項目です")
    private Long skuId;

    private String skuCode;

    @NotBlank(message = "画像URLは必須項目です")
    private String imageUrl;

    private Integer sort;
}
