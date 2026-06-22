package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMakerDTO {
    @NotNull(message = "id is required")
    private Long id;

    @NotBlank(message = "name is required")
    private String name;

    private String englishName;

    @NotNull(message = "seriesId is required")
    private Long seriesId;

    private StatusEnum status;
}
