package co.handk.common.enums;

import lombok.Getter;

@Getter
public enum GoodsBatchActionEnum {

    CREATED("CREATED", "登録しました"),
    UPDATED("UPDATED", "更新しました"),
    FAILED("FAILED", "失敗しました");

    private final String code;
    private final String message;

    GoodsBatchActionEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
