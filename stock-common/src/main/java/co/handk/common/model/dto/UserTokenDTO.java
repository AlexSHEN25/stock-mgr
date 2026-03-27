package co.handk.common.model.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserTokenDTO {

    private Long id;

    private String token;
    private Long userId;
    private LocalDateTime loginTime;
    private LocalDateTime expireTime;
    private String loginIp;
    private Integer status;
}
