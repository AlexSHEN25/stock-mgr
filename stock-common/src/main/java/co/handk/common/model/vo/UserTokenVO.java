package co.handk.common.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserTokenVO extends BaseVO {
    private String token;
    private Long userId;
    private LocalDateTime loginTime;
    private LocalDateTime expireTime;
    private String loginIp;
    private Integer status;
}
