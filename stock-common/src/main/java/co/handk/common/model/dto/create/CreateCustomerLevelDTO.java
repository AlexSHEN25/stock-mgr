package co.handk.common.model.dto.create;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCustomerLevelDTO {

    private String name;
    private BigDecimal discount;
    private String remark;
}
