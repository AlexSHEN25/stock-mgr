package co.handk.common.model.dto.query;

import co.handk.common.model.PageQuery;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockOrderQueryDTO extends PageQuery {

    private Long id;

    private String orderNo;
    private Integer orderType;
    private Integer stockTypeId;
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
