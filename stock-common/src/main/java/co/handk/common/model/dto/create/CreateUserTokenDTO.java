package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateUserTokenDTO {

    @NotBlank(message = "トークンは必須項目です")
    private String token;
    @NotNull(message = "ユーザーIDは必須項目です")
    private Long userId;
    private LocalDateTime loginTime;
    private LocalDateTime expireTime;
    private String loginIp;
}
