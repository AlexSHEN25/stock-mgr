package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateCustomerLevelDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private String name;
    private BigDecimal discount;
    private String remark;
    private StatusEnum status;
}
