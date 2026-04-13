package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCategoryDTO {
    @NotNull(message = "ID荳崎・荳ｺ遨ｺ")
    private Long id;

    private String name;

    private StatusEnum status;
}
