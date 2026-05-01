package co.handk.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum StatusEnum {

    NOMAL(1, "有効"),
    FOBBIDEN(0, "無効");

    private final Integer code;
    private final String message;

    StatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @JsonValue
    public Integer toValue() {
        return this.code;
    }

    @JsonCreator
    public static StatusEnum fromValue(Integer code) {
        if (code == null) {
            return null;
        }
        for (StatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown StatusEnum code: " + code);
    }
}
