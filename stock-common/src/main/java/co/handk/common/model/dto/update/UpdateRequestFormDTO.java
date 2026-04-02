package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UpdateRequestFormDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private String bizNo;
    private Long userId;
    private String username;
    private Long deptId;
    private String deptName;
    private Long customerId;
    private String customerName;
    private Long warehouseId;
    private Integer totalQty;
    private Integer requestQty;
    private Integer state;
    private Long approverId;
    private String approveName;
    private LocalDateTime approveTime;
    private String approveRemark;
}
