package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CreateCustomerLevelDTO {

    private String name;
    private BigDecimal discount;
    private String remark;
}
