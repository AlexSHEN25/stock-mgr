package co.handk.schema.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 外键关联定义（用于下拉、树等）
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaRef {

    /**
     * 关联资源（如 dept）
     */
    String resource() default "";

    /**
     * 显示字段
     */
    String labelField() default "name";

    /**
     * 值字段
     */
    String valueField() default "id";

    /**
     *  前端展示字段名
     */
    String displayField() default "";
    /**
     * 是否多选
     */
    boolean multiple() default false;
}