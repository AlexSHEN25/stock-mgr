package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import co.handk.common.enums.SchemaControlType;
import lombok.Data;

@Data
public class UserVO extends BaseVO {

    @SchemaField(label = "\u30e6\u30fc\u30b6\u30fc\u540d", order = 50, controlType = SchemaControlType.INPUT)
    private String username;
    @SchemaField(label = "\u90e8\u7f72ID", order = 50, controlType = SchemaControlType.SELECT)
    private Long deptId;
    @SchemaField(label = "\u90e8\u7f72\u540d", order = 50, controlType = SchemaControlType.INPUT)
    private String deptName;
    @SchemaField(label = "\u30e1\u30fc\u30eb", order = 50, controlType = SchemaControlType.INPUT)
    private String email;
    @SchemaField(label = "\u96fb\u8a71\u756a\u53f7", order = 50, controlType = SchemaControlType.INPUT)
    private String phone;
    @SchemaField(label = "\u30a2\u30d0\u30bf\u30fc", order = 50, controlType = SchemaControlType.INPUT)
    private String avatar;
    @SchemaField(label = "\u72b6\u614b\u30b3\u30fc\u30c9", order = 40, controlType = SchemaControlType.SELECT)
    private Integer status;
    @SchemaField(label = "\u72b6\u614b", order = 41, controlType = SchemaControlType.INPUT)
    private String statusDesc;

}
