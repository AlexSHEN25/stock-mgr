package co.handk.common.annotation;

import co.handk.common.enums.SchemaControlType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaField {
    String label();

    int order() default 0;

    boolean visible() default true;

    boolean editable() default true;

    SchemaControlType controlType() default SchemaControlType.AUTO;
}
