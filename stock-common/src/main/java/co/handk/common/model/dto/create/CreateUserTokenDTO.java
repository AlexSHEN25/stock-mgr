package co.handk.common.model.dto.create;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateUserTokenDTO {

    private String token;
    private Long userId;
    private LocalDateTime loginTime;
    private LocalDateTime expireTime;
    private String loginIp;
}
