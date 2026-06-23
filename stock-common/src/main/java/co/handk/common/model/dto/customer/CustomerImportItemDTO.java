package co.handk.common.model.dto.customer;

import lombok.Data;

@Data
public class CustomerImportItemDTO {
    private Integer rowNo;
    private Long id;
    private String customerCode;
    private String name;
    private String englishName;
    private String contactPerson;
    private String phone;
    private String email;
    private String country;
    private String city;
    private String address;
    private String levelName;
    private String ownerUserName;
    private String ownerDeptName;
    private String remark;
    private String status;
}
