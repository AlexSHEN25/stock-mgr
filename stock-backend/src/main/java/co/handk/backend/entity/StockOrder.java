package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class StockOrder extends BaseEntity {

    private String orderNo;

    private Integer orderType;

    private Long warehouseId;

    private Integer sourceType;

    private Long sourceId;

    private String idempotencyKey;

    private Integer totalQty;

    private Long stockTypeId;

    private Integer state;

    private Long requesterId;

    private String requesterName;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private Long approverId;

    private String approverName;

    private LocalDateTime approveTime;

    private LocalDate bizDate;

    private Long version;

    private LocalDateTime finishTime;

    private String outboundMode;

    private Long customerId;

    private String customerName;

    private Long deptId;

    private String deptCode;

    private LocalDateTime saleDeadline;
}
