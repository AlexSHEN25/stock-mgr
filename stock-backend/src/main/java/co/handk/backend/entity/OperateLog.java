package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

@Data
@Schema(resource = "operateLog", name = "操作ログ", group = "システム管理/システム設定管理")
public class OperateLog extends BaseEntity {

    @SchemaField(title = "操作用户ID")
    private Long userId;

    @SchemaField(title = "操作用户名")
    private String username;

    @SchemaField(title = "模块")
    private String module;

    @SchemaField(title = "操作类型")
    private String operation;

    @SchemaField(title = "请求方法")
    private String method;

    @SchemaField(title = "请求URL")
    private String requestUrl;

    @SchemaField(title = "请求IP")
    private String requestIp;

    @SchemaField(title = "请求参数")
    private String requestParam;

    @SchemaField(title = "返回数据")
    private String responseData;

    @SchemaField(title = "状态 1成功 0失败")
    private Integer status;

    @SchemaField(title = "错误信息")
    private String errorMsg;

    @SchemaField(title = "执行时间(ms)")
    private Integer costTime;
}