package co.handk.common.model.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class StockOrderDTO {

    private Long id;

    private String orderNo;
    private Integer type;
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
    private LocalDateTime finishTime;
}
