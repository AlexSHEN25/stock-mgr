package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class UpdateCustomerLevelDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private String name;
    private BigDecimal discount;
    private String remark;
    private StatusEnum status;
}
