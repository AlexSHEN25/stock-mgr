package co.handk.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserToken extends BaseEntity {

    /**
     * Token
     */
    private String token;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * token过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 状态
     */
    private Integer status;
}
