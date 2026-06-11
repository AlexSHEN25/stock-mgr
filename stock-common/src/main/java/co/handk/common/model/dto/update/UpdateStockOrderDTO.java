package co.handk.common.model.dto.update;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class UpdateStockOrderDTO {

    @NotNull(message = "IDは必須です")
    private Long id;

    private String orderNo;

    @NotNull(message = "入出庫種別は必須です")
    private Integer orderType;

    @NotNull(message = "倉庫IDは必須です")
    private Long warehouseId;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;
    private LocalDate bizDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;
    private String outboundMode;
    private Long customerId;
    private String customerName;
    private Long deptId;
    private String deptCode;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime saleDeadline;
}
