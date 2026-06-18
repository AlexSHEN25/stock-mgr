package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateMakerDTO {
    @NotNull(message = "id is required")
    private Long id;

    @NotBlank(message = "name is required")
    private String name;

    private StatusEnum status;

    private List<Long> brandIds;
}
