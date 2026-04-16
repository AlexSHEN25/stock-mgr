package co.handk.schema.annotation;

import java.lang.annotation.*;

/**
 * 实体 Schema 定义（低代码入口）
 * 用于标记一个实体为低代码资源
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Schema {

    /**
     * 资源标识（唯一）
     * 对应前端路由 / API路径
     */
    String resource();

    /**
     * 中文名称（菜单显示）
     */
    String name();

    /**
     * 所属分组（菜单分组）
     */
    String group() default "默认分组";

    /**
     * 是否启用
     */
    boolean enabled() default true;

    /**
     * 排序
     */
    int order() default 0;

    /**
     * API 前缀（默认 /api/{resource}）
     */
    String apiPrefix() default "";

    /**
     * 权限前缀（用于RBAC）
     */
    String permissionPrefix() default "";

    /**
     * 描述
     */
    String description() default "";
}