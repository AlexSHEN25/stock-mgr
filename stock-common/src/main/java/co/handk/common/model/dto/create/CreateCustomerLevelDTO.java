package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCustomerLevelDTO {

    @NotBlank(message = "名称は必須項目です")
    private String name;
    private BigDecimal discount;
    private String remark;
}
