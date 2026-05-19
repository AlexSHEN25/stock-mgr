package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateGoodsImageDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

    @NotNull(message = "商品は必須項目です")
    private Long goodsId;

    @NotNull(message = "SKUは必須項目です")
    private Long skuId;

    private String skuCode;

    @NotBlank(message = "画像URLは必須項目です")
    private String imageUrl;

    private Integer sort;
}
