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
     * 逕ｨ謌ｷID
     */
    private Long userId;

    /**
     * 逋ｻ蠖墓慮髣ｴ
     */
    private LocalDateTime loginTime;

    /**
     * token霑・悄譌ｶ髣ｴ
     */
    private LocalDateTime expireTime;

    /**
     * 逋ｻ蠖肘P
     */
    private String loginIp;

    /**
     * 迥ｶ諤・
     */
    private Integer status;
}
