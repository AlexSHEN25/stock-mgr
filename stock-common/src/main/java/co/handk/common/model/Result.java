package co.handk.common.model;

import co.handk.common.enums.ResultCode;
import lombok.Getter;

@Getter
public class Result<T> {

    private Integer code;
    private String messageKey;
    private String message;
    private T data;

    private Result(Integer code, String messageKey, String message, T data) {
        this.code = code;
        this.messageKey = messageKey;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> fail(ResultCode code) {
        return new Result<>(code.getCode(), code.getMessageKey(), code.getMessage(), null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessageKey(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessageKey(), message, data);
    }

    public static <T> Result<T> fail(ResultCode code, String message) {
        return new Result<>(code.getCode(), code.getMessageKey(), message, null);
    }

    public static <T> Result<T> fail(ResultCode code, String messageKey, String message) {
        return new Result<>(code.getCode(), messageKey, message, null);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, null, message, null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessageKey(), message, null);
    }

    public static <T> Result<T> error() {
        return new Result<>(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessageKey(), ResultCode.ERROR.getMessage(), null);
    }
}
