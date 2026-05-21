package co.handk.common.model.dto.create;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import co.handk.common.enums.StatusEnum;
import lombok.Data;

@Data
public class CreateUserDTO {

    @NotBlank(message = "必須項目です")
    private String username;

    @NotNull(message = "必須項目です")
    private Long deptId;

    @NotBlank(message = "必須項目です")
    private String password;

    @Email(message = "メール形式が正しくありません")
    private String email;

    private String phone;
    private String avatar;
    private StatusEnum status;

}
