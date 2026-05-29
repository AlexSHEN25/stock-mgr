package co.handk.common.model.dto.create;

import lombok.Data;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateBrandMakerRelationDTO {

    private Long brandId;

    private Long makerId;
}
