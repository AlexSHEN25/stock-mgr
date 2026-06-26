package co.handk.common.model.dto.query;

import co.handk.common.model.PageQuery;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StockOrderQueryDTO extends PageQuery {


    private String orderNo;
    private Integer orderType;
    private String stockCategory;
    private Long stockTypeId;
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
    private LocalDate bizDate;
    private LocalDateTime finishTime;
    private String outboundMode;
    private Long customerId;
    private String customerName;
    private Long deptId;
    private String deptCode;
    private LocalDateTime saleDeadline;
}
