package co.handk.common.enums;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "success"),
    ERROR(500, "server error"),
    VALIDATE_ERROR(400, "param error");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
