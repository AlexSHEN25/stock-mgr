package co.handk.backend.annotation;

import co.handk.backend.enums.JoinType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinTable {
    JoinType type() default JoinType.LEFT;

    String table();

    String alias() default "";

    /**
     * Join ON expression, ignored for CROSS JOIN.
     */
    String on() default "";
}

