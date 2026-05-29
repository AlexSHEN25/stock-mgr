package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import co.handk.common.enums.StatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateCustomerLevelDTO {

    @NotBlank(message = "必須項目です")
    private String name;
    @PositiveOrZero(message = "0以上で入力してください")
    private BigDecimal discount;
    private String remark;
    private StatusEnum status;

}
