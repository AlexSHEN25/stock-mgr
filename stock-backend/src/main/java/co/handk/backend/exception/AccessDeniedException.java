package co.handk.backend.exception;

import lombok.Getter;

@Getter
public class AccessDeniedException extends RuntimeException {

    private final String messageKey;

    public AccessDeniedException(String messageKey, String message) {
        super(message);
        this.messageKey = messageKey;
    }

}

