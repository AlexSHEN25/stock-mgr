package co.handk.backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinQueryConfig {

    String baseTable();

    String baseAlias() default "t";

    JoinTable[] joins() default {};
}

