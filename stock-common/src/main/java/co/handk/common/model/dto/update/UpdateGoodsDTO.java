package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateGoodsDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private String name;
    private String englishName;
    private String skuCode;
    private Long seriesId;
    private Long brandId;
    private Long categoryId;
    private Long makerId;
    private String description;
    private Integer isHot;
    private Integer sort;
    private StatusEnum status;
}
