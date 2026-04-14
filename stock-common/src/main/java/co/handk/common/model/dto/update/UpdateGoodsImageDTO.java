package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateGoodsImageDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private Long goodsId;

    private Long skuId;

    private String skuCode;

    private String imageUrl;

    private Integer sort;
}
