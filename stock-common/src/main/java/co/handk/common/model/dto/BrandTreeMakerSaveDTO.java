package co.handk.common.model.dto;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrandTreeMakerSaveDTO {

    private Long id;

    @NotBlank(message = "maker name is required")
    private String name;

    private String englishName;

    private StatusEnum status;
}
