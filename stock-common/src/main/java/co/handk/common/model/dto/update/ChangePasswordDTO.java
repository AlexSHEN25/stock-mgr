package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordDTO {

    @NotBlank(message = "必須項目です")
    private String password;
}
