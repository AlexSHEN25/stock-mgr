package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import co.handk.common.enums.StatusEnum;
import lombok.Data;

import java.time.LocalDateTime;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateUserTokenDTO {

    @NotBlank(message = "必須項目です")
    private String token;
    @NotNull(message = "必須項目です")
    private Long userId;
    private LocalDateTime loginTime;
    private LocalDateTime expireTime;
    private String loginIp;
    private StatusEnum status;

}
