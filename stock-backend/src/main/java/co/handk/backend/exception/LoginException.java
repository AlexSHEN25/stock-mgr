package co.handk.backend.exception;

import lombok.Getter;

@Getter
public class LoginException extends RuntimeException {

    private final String messageKey;

    public LoginException(String messageKey, String message) {
        super(message);
        this.messageKey = messageKey;
    }

}
