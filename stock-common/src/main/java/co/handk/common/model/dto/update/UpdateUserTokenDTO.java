package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UpdateUserTokenDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private String token;
    private Long userId;
    private LocalDateTime loginTime;
    private LocalDateTime expireTime;
    private String loginIp;
    private StatusEnum status;
}
