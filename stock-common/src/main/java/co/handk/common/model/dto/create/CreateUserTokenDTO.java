package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CreateUserTokenDTO {

    private String token;
    private Long userId;
    private LocalDateTime loginTime;
    private LocalDateTime expireTime;
    private String loginIp;
    private StatusEnum status;
}
