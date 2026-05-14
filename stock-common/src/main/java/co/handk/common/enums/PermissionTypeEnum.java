package co.handk.common.enums;

import lombok.Getter;

@Getter
public enum PermissionTypeEnum {
    MENU(1),
    DATA(2),
    API(3);

    private final Integer code;

    PermissionTypeEnum(Integer code) {
        this.code = code;
    }
}

