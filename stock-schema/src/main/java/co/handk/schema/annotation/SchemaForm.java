package co.handk.schema.annotation;

import java.lang.annotation.*;

/**
 * 表单扩展配置
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaForm {

    /**
     * 栅格占位（24栅格）
     */
    int span() default 24;

    /**
     * 分组（用于分区显示）
     */
    String group() default "";

    /**
     * 是否隐藏
     */
    boolean hidden() default false;
}