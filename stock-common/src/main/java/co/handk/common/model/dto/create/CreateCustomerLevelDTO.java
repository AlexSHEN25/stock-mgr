package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCustomerLevelDTO {

    private String name;
    private BigDecimal discount;
    private String remark;
    private StatusEnum status;
}
