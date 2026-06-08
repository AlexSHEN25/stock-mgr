package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateRequestFormDTO {

    private Long userId;

    private String username;

    private Long deptId;

    private String deptName;

    @NotNull(message = "customerId is required")
    private Long customerId;

    private String customerName;

    private Long warehouseId;

    private Long sourceOrderId;

    @PositiveOrZero(message = "value must be greater than or equal to 0")
    private Integer totalQty;

    @PositiveOrZero(message = "value must be greater than or equal to 0")
    private Integer requestQty;

    @PositiveOrZero(message = "value must be greater than or equal to 0")
    private BigDecimal totalAmt;

    private Integer state;

    private Long approverId;

    private String approverName;

    private LocalDateTime approveTime;

    private String approveRemark;
}
