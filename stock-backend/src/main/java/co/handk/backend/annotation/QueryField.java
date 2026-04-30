package co.handk.backend.annotation;

import co.handk.common.enums.QueryType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryField {

    String column() default "";

    QueryType type() default QueryType.EQ;

    // 是否忽略 null
    boolean ignoreNull() default true;

}