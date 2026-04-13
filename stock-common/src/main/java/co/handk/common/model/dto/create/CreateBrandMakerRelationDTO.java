package co.handk.common.model.dto.create;

import lombok.Data;

@Data
public class CreateBrandMakerRelationDTO {

    private Long brandId;

    private Long makerId;
}
