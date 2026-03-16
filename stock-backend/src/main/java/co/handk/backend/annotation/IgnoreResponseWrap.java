package co.handk.backend.annotation;

import java.lang.annotation.*;


/**
 * 有些接口（文件下载、第三方回调）需要原始返回，就加这个注解。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreResponseWrap {
}