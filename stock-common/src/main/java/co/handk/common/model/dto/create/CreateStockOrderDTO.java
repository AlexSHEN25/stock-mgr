package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateStockOrderDTO {

    @NotBlank(message = "注文番号は必須です")
    private String orderNo;

    @NotNull(message = "入出庫種別は必須です")
    private Integer orderType;

    @NotNull(message = "倉庫IDは必須です")
    private Long warehouseId;

    @NotNull(message = "ソース種別は必須です")
    private Integer sourceType;

    private Long sourceId;
    @PositiveOrZero(message = "0以上で入力してください")
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
    private LocalDateTime bizDate;
    private LocalDateTime finishTime;
}
