package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateStockOrderDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private String orderNo;
    private Integer orderType;
    private Long warehouseId;
    private Integer sourceType;
    private Long sourceId;
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
    private LocalDateTime finishTime;
}
