package co.handk.common.enums;

import lombok.Getter;

@Getter
public enum PermissionTypeEnum implements OptionEnum {
    MENU(1, "メニュー"),
    DATA(2, "データ"),
    API(3, "API");

    private final Integer code;
    private final String label;

    PermissionTypeEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }
}
