package co.handk.common.model.dto.create;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserDTO {

    @NotBlank(message = "ユーザー名は必須項目です")
    private String username;

    @NotNull(message = "部署は必須項目です")
    private Long deptId;

    @NotBlank(message = "パスワードは必須項目です")
    private String password;

    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;

    private String phone;
    private String avatar;
}
