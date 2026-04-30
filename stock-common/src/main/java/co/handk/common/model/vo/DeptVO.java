package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

@Data
public class DeptVO extends BaseVO {
    @SchemaField(label = "\u89aaID", order = 50)
    private Long parentId;
    @SchemaField(label = "\u540d\u79f0", order = 50)
    private String name;
    @SchemaField(label = "\u30b3\u30fc\u30c9", order = 50)
    private String code;
    @SchemaField(label = "\u8cac\u4efb\u8005ID", order = 50)
    private Long leaderId;
    @SchemaField(label = "\u4e26\u3073\u9806", order = 50)
    private Integer sort;
    @SchemaField(label = "\u72b6\u614b\u30b3\u30fc\u30c9", order = 40)
    private Integer status;
    @SchemaField(label = "\u72b6\u614b", order = 41)
    private String statusDesc;
}
