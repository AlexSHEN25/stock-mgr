package co.handk.schema.annotation;

import co.handk.schema.enums.Align;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表格扩展配置
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaTable {

    /**
     * 是否固定列
     */
    boolean fixed() default false;

    /**
     * 是否省略显示
     */
    boolean ellipsis() default false;

    /**
     * 对齐方式
     */
    Align align() default Align.LEFT;
}