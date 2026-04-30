package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

@Data
public class WarehouseVO extends BaseVO {
    @SchemaField(label = "\u540d\u79f0", order = 50)
    private String name;
    @SchemaField(label = "\u30b3\u30fc\u30c9", order = 50)
    private String code;
    @SchemaField(label = "\u4f4f\u6240", order = 50)
    private String address;
    @SchemaField(label = "\u7ba1\u7406\u8005ID", order = 50)
    private Long managerId;
    @SchemaField(label = "\u7ba1\u7406\u8005\u540d", order = 50)
    private String managerName;
    @SchemaField(label = "\u72b6\u614b\u30b3\u30fc\u30c9", order = 40)
    private Integer status;
    @SchemaField(label = "\u72b6\u614b", order = 41)
    private String statusDesc;
}
