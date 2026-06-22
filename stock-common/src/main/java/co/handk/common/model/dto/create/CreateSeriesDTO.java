package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSeriesDTO {

    @NotBlank(message = "name is required")
    private String name;

    private String englishName;

    @NotNull(message = "brandId is required")
    private Long brandId;

    private String content;

    private StatusEnum status;
}
