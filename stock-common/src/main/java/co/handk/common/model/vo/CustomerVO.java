package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

@Data
public class CustomerVO extends BaseVO {
    @SchemaField(label = "\u9867\u5ba2\u30b3\u30fc\u30c9", order = 50)
    private String customerCode;
    @SchemaField(label = "\u540d\u79f0", order = 50)
    private String name;
    @SchemaField(label = "\u82f1\u8a9e\u540d", order = 50)
    private String englishName;
    @SchemaField(label = "\u62c5\u5f53\u8005", order = 50)
    private String contactPerson;
    @SchemaField(label = "\u96fb\u8a71\u756a\u53f7", order = 50)
    private String phone;
    @SchemaField(label = "\u30e1\u30fc\u30eb", order = 50)
    private String email;
    @SchemaField(label = "\u56fd", order = 50)
    private String country;
    @SchemaField(label = "\u5e02\u533a\u753a\u6751", order = 50)
    private String city;
    @SchemaField(label = "\u4f4f\u6240", order = 50)
    private String address;
    @SchemaField(label = "\u30e9\u30f3\u30afID", order = 50)
    private Integer levelId;
    @SchemaField(label = "\u30e9\u30f3\u30af\u540d", order = 50)
    private String levelName;
    @SchemaField(label = "\u62c5\u5f53\u30e6\u30fc\u30b6\u30fcID", order = 50)
    private Long ownerUserId;
    @SchemaField(label = "\u62c5\u5f53\u30e6\u30fc\u30b6\u30fc\u540d", order = 50)
    private String ownerUserName;
    @SchemaField(label = "\u62c5\u5f53\u90e8\u7f72ID", order = 50)
    private Long ownerDeptId;
    @SchemaField(label = "\u5099\u8003", order = 50)
    private String remark;
    @SchemaField(label = "\u72b6\u614b\u30b3\u30fc\u30c9", order = 40)
    private Integer status;
    @SchemaField(label = "\u72b6\u614b", order = 41)
    private String statusDesc;
}
