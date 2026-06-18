package co.handk.common.model.dto.create;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockGroupAllocationItemDTO {

    private Long deptId;

    private String groupCode;

    private String deptCode;

    @NotNull(message = "数量は必須です")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;
}
