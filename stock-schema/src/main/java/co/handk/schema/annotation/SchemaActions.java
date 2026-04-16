package co.handk.schema.annotation;

import java.lang.annotation.*;

/**
 * 支持多个 SchemaAction
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaActions {

    SchemaAction[] value();
}