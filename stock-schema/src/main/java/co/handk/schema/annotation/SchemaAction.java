package co.handk.schema.annotation;

import co.handk.schema.enums.ActionType;
import co.handk.schema.enums.HttpMethod;

import java.lang.annotation.*;

/**
 * 页面操作定义（按钮/接口行为）
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SchemaActions.class)
public @interface SchemaAction {

    /**
     * 操作编码（唯一）
     */
    String code();

    /**
     * 操作名称
     */
    String name();

    /**
     * 操作类型（按钮/弹窗等）
     */
    ActionType type() default ActionType.BUTTON;

    /**
     * 请求API
     */
    String api();

    /**
     * 请求方法
     */
    HttpMethod method() default HttpMethod.GET;

    /**
     * 是否支持批量操作
     */
    boolean batch() default false;

    /**
     * 是否需要确认框
     */
    boolean confirm() default false;

    /**
     * 权限标识
     */
    String permission() default "";
}