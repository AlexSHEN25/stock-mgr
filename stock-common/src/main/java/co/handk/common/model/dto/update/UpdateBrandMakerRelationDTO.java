package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class UpdateBrandMakerRelationDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

    private Long brandId;

    private Long makerId;
}
