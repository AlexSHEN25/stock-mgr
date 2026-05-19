package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotBlank;
import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateUserTokenDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

    @NotBlank(message = "トークンは必須項目です")
    private String token;
    @NotNull(message = "ユーザーIDは必須項目です")
    private Long userId;
    private LocalDateTime loginTime;
    private LocalDateTime expireTime;
    private String loginIp;
    private StatusEnum status;
}
