package co.handk.backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Join {

    /**
     * 直接写 SQL JOIN 片段
     * 例：LEFT JOIN dept d ON t.dept_id = d.id
     */
    String value();
}
