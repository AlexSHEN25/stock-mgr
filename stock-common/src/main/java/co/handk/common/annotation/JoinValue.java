package co.handk.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinValue {

    /**
     * 当前 VO 上作为关联主键来源的字段名，例如 brandId。
     */
    String sourceField();

    /**
     * Spring Bean 名称，例如 brandServiceImpl。
     * 该 Bean 需提供 getByIdNotDeleted(Serializable id) 方法。
     */
    String serviceBean();

    /**
     * 关联对象上用于回填的字段名，例如 name。
     */
    String targetField();
}

