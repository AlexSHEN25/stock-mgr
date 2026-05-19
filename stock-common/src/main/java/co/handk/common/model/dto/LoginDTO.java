package co.handk.common.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {

    @NotBlank(message = "ユーザー名は必須項目です")
    private String username;

    @NotBlank(message = "パスワードは必須項目です")
    private String password;
}
