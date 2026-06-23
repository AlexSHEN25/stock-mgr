package co.handk.common.model.vo;

import co.handk.common.annotation.JoinValue;
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
    @JoinValue(sourceField = "levelId", serviceBean = "customerLevelServiceImpl", targetField = "name")
    private String levelName;
    private Long ownerUserId;
    @JoinValue(sourceField = "ownerUserId", serviceBean = "userServiceImpl", targetField = "username")
    private String ownerUserName;
    private Long ownerDeptId;
    @JoinValue(sourceField = "ownerDeptId", serviceBean = "deptServiceImpl", targetField = "name")
    private String ownerDeptName;
    private String remark;
    private Integer status;
    private String statusDesc;
}
