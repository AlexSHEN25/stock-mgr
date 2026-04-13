package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateGoodsImageDTO {
    @NotNull(message = "ID荳崎・荳ｺ遨ｺ")
    private Long id;

    private Long goodsId;

    private Long skuId;

    private String skuCode;

    private String imageUrl;

    private Integer sort;
}
