package co.handk.common.model.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CustomerVO {

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
