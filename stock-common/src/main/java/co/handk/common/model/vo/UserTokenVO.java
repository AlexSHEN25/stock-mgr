package co.handk.common.model.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserTokenVO {

    private Long id;

    private String token;
    private Long userId;
    private LocalDateTime loginTime;
    private LocalDateTime expireTime;
    private String loginIp;
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
