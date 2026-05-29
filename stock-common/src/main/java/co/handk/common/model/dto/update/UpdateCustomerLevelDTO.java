package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class UpdateCustomerLevelDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

    @NotBlank(message = "名称は必須項目です")
    private String name;
    @PositiveOrZero(message = "0以上で入力してください")
    private BigDecimal discount;
    private String remark;
    private StatusEnum status;
}
