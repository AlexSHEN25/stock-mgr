package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

@Data
@Schema(resource = "customer", name = "顧客", group = "システム管理/顧客管理")
public class Customer extends BaseEntity {

    @SchemaField(title = "客户编号")
    private String customerCode;

    @SchemaField(title = "客户名称")
    private String name;

    @SchemaField(title = "英文名称")
    private String englishName;

    @SchemaField(title = "联系人")
    private String contactPerson;

    @SchemaField(title = "联系电话")
    private String phone;

    @SchemaField(title = "邮箱")
    private String email;

    @SchemaField(title = "国家")
    private String country;

    @SchemaField(title = "城市")
    private String city;

    @SchemaField(title = "详细地址")
    private String address;

    @SchemaField(title = "客户等级")
    private Integer levelId;

    @SchemaField(title = "归属负责人ID")
    private Long ownerUserId;

    @SchemaField(title = "归属部门ID")
    private Long ownerDeptId;

    @SchemaField(title = "备注")
    private String remark;

    @SchemaField(title = "状态(1正常0停用)")
    private Integer status;
}