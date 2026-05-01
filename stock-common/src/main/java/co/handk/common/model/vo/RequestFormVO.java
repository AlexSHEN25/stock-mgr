package co.handk.common.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequestFormVO extends BaseVO {
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
