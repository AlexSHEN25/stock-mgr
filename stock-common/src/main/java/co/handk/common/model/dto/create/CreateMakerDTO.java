package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateMakerDTO {

    @NotBlank(message = "名称は必須項目です")
    private String name;
}
