package co.handk.backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Join {

    /**
     * 逶ｴ謗･蜀・SQL JOIN 迚・ｮｵ
     * 萓具ｼ哭EFT JOIN dept d ON t.dept_id = d.id
     */
    String value();
}
