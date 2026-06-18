package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateMakerDTO {

    @NotBlank(message = "name is required")
    private String name;

    private StatusEnum status;

    private List<Long> brandIds;
}
