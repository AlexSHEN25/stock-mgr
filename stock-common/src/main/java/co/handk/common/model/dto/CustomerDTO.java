package co.handk.common.model.dto;

import lombok.Data;

@Data
public class CustomerDTO {

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
    private Integer levelId;
    private String remark;
    private Integer status;
}
