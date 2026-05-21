package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserDTO {

    @NotNull(message = "IDは必須です")
    private Long id;

    @NotBlank(message = "ユーザー名は必須です")
    private String username;

    private Long deptId;
    private String email;
    private String phone;
    private String avatar;
    private StatusEnum status;
}
