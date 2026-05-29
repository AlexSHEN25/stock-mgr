package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateRequestFormDTO {

    @NotNull(message = "ユーザーIDは必須です")
    private Long userId;

    @NotBlank(message = "ユーザー名は必須です")
    private String username;

    private Long deptId;

    private String deptName;

    @NotNull(message = "顧客IDは必須です")
    private Long customerId;

    @NotBlank(message = "顧客名は必須です")
    private String customerName;

    private Long warehouseId;

    private Long sourceOrderId;

    @PositiveOrZero(message = "0以上で入力してください")
    private Integer totalQty;

    @PositiveOrZero(message = "0以上で入力してください")
    private Integer requestQty;

    @PositiveOrZero(message = "0以上で入力してください")
    private BigDecimal totalAmt;

    private Integer state;

    private Long approverId;

    private String approverName;

    private LocalDateTime approveTime;

    private String approveRemark;
}
