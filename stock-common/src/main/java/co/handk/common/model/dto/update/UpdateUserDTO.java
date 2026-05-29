package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class UpdateUserDTO {

    @NotNull(message = "IDは必須です")
    private Long id;

    @NotBlank(message = "ユーザー名は必須です")
    private String username;

    private String password;
    private Long deptId;
    private Long roleId;
    private String email;
    private String phone;
    private String avatar;
    private StatusEnum status;
}
