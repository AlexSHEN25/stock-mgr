package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserDTO {

    @NotNull(message = "IDは必須項目です")
    private Long id;
    @NotBlank(message = "ユーザー名は必須項目です")
    private String username;

    private Long deptId;
    @NotBlank(message = "パスワードは必須項目です")
    private String password;
    private String email;
    private String phone;
    private String avatar;
    private StatusEnum status;
}
