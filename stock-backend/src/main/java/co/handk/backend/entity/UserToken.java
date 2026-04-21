package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(resource = "userToken", name = "ユーザーログイン状態", group = "システム管理/ユーザー管理")
public class UserToken extends BaseEntity {

    /**
     * Token
     */
    @SchemaField(title = "Token")
    private String token;

    /**
     * 用户ID
     */
    @SchemaField(title = "用户ID")
    private Long userId;

    /**
     * 登录时间
     */
    @SchemaField(title = "登录时间")
    private LocalDateTime loginTime;

    /**
     * token过期时间
     */
    @SchemaField(title = "token过期时间")
    private LocalDateTime expireTime;

    /**
     * 登录IP
     */
    @SchemaField(title = "登录IP")
    private String loginIp;

    /**
     * 状态
     */
    @SchemaField(title = "状态")
    private Integer status;
}