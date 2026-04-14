package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateGoodsSkuSpecDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private Long skuId;

    private String skuCode;

    private Long specId;

    private String specName;

    private String specValue;

    private Integer sort;
}
