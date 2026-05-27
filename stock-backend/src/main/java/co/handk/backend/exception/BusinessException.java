package co.handk.backend.exception;

import co.handk.backend.constant.MessageKeyConstant;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String messageKey;

    public BusinessException(String message) {
        super(message);
        this.messageKey = MessageKeyConstant.ERROR_RUNTIME;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.messageKey = MessageKeyConstant.ERROR_RUNTIME;
    }

    public BusinessException(String messageKey, String message) {
        super(message);
        this.messageKey = messageKey;
    }

    public BusinessException(String messageKey, String message, Throwable cause) {
        super(message, cause);
        this.messageKey = messageKey;
    }

}
