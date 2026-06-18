package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserToken extends BaseEntity {

    /**
     * Token
     */
    private String token;

    /**
     * ユーザーID
     */
    private Long userId;

    /**
     * ログイン時間
     */
    private LocalDateTime loginTime;

    /**
     * Token有効期限
     */
    private LocalDateTime expireTime;

    /**
     * ログインIP
     */
    private String loginIp;

    /**
     * 状態
     */
    private Integer status;
}
