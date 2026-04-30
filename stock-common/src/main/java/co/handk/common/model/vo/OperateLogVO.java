package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

@Data
public class OperateLogVO extends BaseVO {
    @SchemaField(label = "\u30e6\u30fc\u30b6\u30fcID", order = 50)
    private Long userId;
    @SchemaField(label = "\u30e6\u30fc\u30b6\u30fc\u540d", order = 50)
    private String username;
    @SchemaField(label = "\u30e2\u30b8\u30e5\u30fc\u30eb", order = 50)
    private String module;
    @SchemaField(label = "\u64cd\u4f5c", order = 50)
    private String operation;
    @SchemaField(label = "\u30e1\u30bd\u30c3\u30c9", order = 50)
    private String method;
    @SchemaField(label = "\u30ea\u30af\u30a8\u30b9\u30c8URL", order = 50)
    private String requestUrl;
    @SchemaField(label = "\u30ea\u30af\u30a8\u30b9\u30c8IP", order = 50)
    private String requestIp;
    @SchemaField(label = "\u30ea\u30af\u30a8\u30b9\u30c8\u30d1\u30e9\u30e1\u30fc\u30bf", order = 50)
    private String requestParam;
    @SchemaField(label = "\u30ec\u30b9\u30dd\u30f3\u30b9\u30c7\u30fc\u30bf", order = 50)
    private String responseData;
    @SchemaField(label = "\u72b6\u614b\u30b3\u30fc\u30c9", order = 40)
    private Integer status;
    @SchemaField(label = "\u30a8\u30e9\u30fc\u30e1\u30c3\u30bb\u30fc\u30b8", order = 50)
    private String errorMsg;
    @SchemaField(label = "costTime", order = 50)
    private Integer costTime;
}
