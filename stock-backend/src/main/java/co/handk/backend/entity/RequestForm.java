package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class RequestForm extends BaseEntity {

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

    private BigDecimal totalAmt;

    private Integer state;

    private Long approverId;

    private String approverName;

    private LocalDateTime approveTime;

    private String approveRemark;
}
