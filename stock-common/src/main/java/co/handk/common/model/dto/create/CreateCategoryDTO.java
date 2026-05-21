package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import co.handk.common.enums.StatusEnum;
import lombok.Data;

@Data
public class CreateCategoryDTO {

    @NotBlank(message = "必須項目です")
    private String name;

    private StatusEnum status;

}
