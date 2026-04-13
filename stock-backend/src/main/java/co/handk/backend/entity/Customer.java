package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Customer extends BaseEntity {

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

    private Long ownerUserId;

    private Long ownerDeptId;

    private String remark;

    private Integer status;
}
