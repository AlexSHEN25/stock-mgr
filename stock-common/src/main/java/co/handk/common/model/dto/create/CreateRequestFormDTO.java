package co.handk.common.model.dto.create;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateRequestFormDTO {

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
    private String approverName;
    private LocalDateTime approveTime;
    private String approveRemark;
}
