package co.handk.backend.exception;

public class AccessDeniedException extends RuntimeException {

    private final String messageKey;

    public AccessDeniedException(String messageKey, String message) {
        super(message);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}

