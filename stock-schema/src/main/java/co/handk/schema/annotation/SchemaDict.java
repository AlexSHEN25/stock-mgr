package co.handk.schema.annotation;

import java.lang.annotation.*;

/**
 * 字典定义（枚举、状态等）
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaDict {

    /**
     * 字典编码
     */
    String code() default "";

    /**
     * 是否远程加载
     */
    boolean remote() default false;
}