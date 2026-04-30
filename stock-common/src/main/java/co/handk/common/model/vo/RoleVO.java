package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

@Data
public class RoleVO extends BaseVO {
    @SchemaField(label = "\u540d\u79f0", order = 50)
    private String name;
    @SchemaField(label = "\u30b3\u30fc\u30c9", order = 50)
    private String code;
    @SchemaField(label = "\u5099\u8003", order = 50)
    private String remark;
    @SchemaField(label = "\u72b6\u614b\u30b3\u30fc\u30c9", order = 40)
    private Integer status;
    @SchemaField(label = "\u72b6\u614b", order = 41)
    private String statusDesc;
}
