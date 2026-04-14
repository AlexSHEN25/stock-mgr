package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateBrandMakerRelationDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private Long brandId;

    private Long makerId;
}
