package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateStockOrderDTO {

    @NotBlank(message = "伝票番号は必須項目です")
    private String orderNo;
    @NotNull(message = "入出庫種別は必須項目です")
    private Integer orderType;
    @NotNull(message = "倉庫は必須項目です")
    private Long warehouseId;
    @NotNull(message = "入出庫ソースは必須項目です")
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
    private LocalDateTime approveTime;
    private LocalDateTime finishTime;
}
