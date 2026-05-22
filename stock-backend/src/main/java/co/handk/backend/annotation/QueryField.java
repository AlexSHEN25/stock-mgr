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

    // true の場合、null 値はクエリ条件に含めない
    boolean ignoreNull() default true;
}
