package co.handk.common.model.vo;

import lombok.Data;

@Data
public class CustomerVO extends BaseVO {
    private String customerCode;
    private String name;
    private String englishName;
    private String contactPerson;
    private String phone;
    private String email;
    private String country;
    private String city;
    private String address;
    private Integer levelId;
    private String levelName;
    private Long ownerUserId;
    private String ownerUserName;
    private Long ownerDeptId;
    private String remark;
    private Integer status;
    private String statusDesc;
}
