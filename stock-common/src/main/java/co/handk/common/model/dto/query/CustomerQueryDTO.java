package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class CustomerQueryDTO extends PageQuery {

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
    private Long ownerUserId;
    private Long ownerDeptId;
    private String remark;
    private StatusEnum status;
}
