package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCustomerDTO {
    @NotNull(message = "ID不能为空")
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
    private StatusEnum status;
}
