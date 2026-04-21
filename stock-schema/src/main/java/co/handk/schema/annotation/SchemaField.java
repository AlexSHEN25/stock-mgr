package co.handk.schema.annotation;

import co.handk.schema.enums.FieldType;
import co.handk.schema.enums.QueryType;

import java.lang.annotation.*;

/**
 * 字段 Schema 定义
 * 控制前端表单、表格、查询行为
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SchemaField {

    /**
     * 字段标题（前端显示名称）
     */
    String title();

    /**
     * 前端组件类型
     */
    FieldType type() default FieldType.INPUT;

    /**
     * 是否必填
     */
    boolean required() default false;

    /**
     * 是否在表格中显示
     */
    boolean table() default true;

    /**
     * 是否可搜索
     */
    boolean search() default false;

    /**
     * 查询方式（= like between）
     */
    QueryType queryType() default QueryType.EQ;

    /**
     * 是否可排序
     */
    boolean sortable() default false;

    /**
     * 是否可编辑
     */
    boolean editable() default true;

    /**
     * 是否在详情中显示
     */
    boolean detail() default true;

    /**
     * 默认值
     */
    String defaultValue() default "";

    /**
     * 输入提示
     */
    String placeholder() default "";

    /**
     * 表格宽度
     */
    int width() default 150;

    /**
     * 字典配置（状态、枚举）
     */
    SchemaDict dict() default @SchemaDict;

    /**
     * 关联配置（外键）
     */
    SchemaRef ref() default @SchemaRef;

    /**
     * 表单扩展配置
     */
    SchemaForm form() default @SchemaForm;

    /**
     * 表格扩展配置
     */
    SchemaTable tableExt() default @SchemaTable;

    /**
     * 排序
     */
    int order() default 0;
}