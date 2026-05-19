package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateRequestFormDTO {

    @NotNull(message = "IDは必須です")
    private Long id;

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

    private Integer totalQty;

    private Integer requestQty;

    private BigDecimal totalAmt;

    private Integer state;

    private Long approverId;

    private String approverName;

    private LocalDateTime approveTime;

    private String approveRemark;
}