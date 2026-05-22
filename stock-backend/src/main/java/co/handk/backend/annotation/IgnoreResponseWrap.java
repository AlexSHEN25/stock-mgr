package co.handk.backend.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * このアノテーションを付けたクラス/メソッドは
 * グローバルレスポンスラップ(Result.success)の対象外にする。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreResponseWrap {
}
