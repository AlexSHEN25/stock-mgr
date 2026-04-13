package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class StockOrder extends BaseEntity {

    private String orderNo;

    private Integer orderType;

    private Integer typeId;

    private Long warehouseId;

    private Integer sourceType;

    private Long sourceId;

    private Integer totalQty;

    private Integer state;

    private Long requesterId;

    private String requesterName;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private Long approverId;

    private String approverName;

    private LocalDateTime approveTime;

    private Long version;

    private LocalDateTime finishTime;
}
