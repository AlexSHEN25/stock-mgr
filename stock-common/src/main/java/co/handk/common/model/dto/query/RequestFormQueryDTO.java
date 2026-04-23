package co.handk.common.model.dto.query;

import java.time.LocalDateTime;
import lombok.Data;
import co.handk.common.model.PageQuery;

@Data
public class RequestFormQueryDTO extends PageQuery {

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
    private String approverName;
    private LocalDateTime approveTime;
    private String approveRemark;
}
