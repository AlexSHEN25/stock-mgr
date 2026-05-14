package co.handk.common.enums;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "success", "result.success"),
    ERROR(500, "server error", "result.error"),
    VALIDATE_ERROR(400, "param error", "result.validate_error"),
    LOGIN_TIME_OUT(401, "login timeout", "result.login_timeout");

    private final Integer code;
    private final String message;
    private final String messageKey;

    ResultCode(Integer code, String message, String messageKey) {
        this.code = code;
        this.message = message;
        this.messageKey = messageKey;
    }

}
