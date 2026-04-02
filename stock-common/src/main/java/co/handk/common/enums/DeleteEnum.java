package co.handk.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum DeleteEnum {

    DELETED(1, "deleted"),
    UNDELETED(0, "undeleted");

    private final Integer code;
    private final String message;

    DeleteEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @JsonValue
    public Integer toValue() {
        return this.code;
    }

    @JsonCreator
    public static DeleteEnum fromValue(Integer code) {
        if (code == null) {
            return null;
        }
        for (DeleteEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown DeleteEnum code: " + code);
    }
}
