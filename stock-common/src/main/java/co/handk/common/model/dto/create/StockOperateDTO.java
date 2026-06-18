package co.handk.common.model.dto.create;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockOperateDTO {

    private Long stockId;

    private Integer goodsId;

    private Long skuId;

    private Integer warehouseId;

    private Long stockTypeId;

    @NotNull(message = "数量は必須です")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;

    /**
     * Inbound only:
     * 1: self inbound (approval required)
     * 2: resale inbound (immediate)
     */
    private Integer sourceType;

    private Long customerId;

    private String customerName;

    private Long deptId;

    private String groupCode;

    private String deptCode;

    private Integer groupAQty;

    private Integer groupBQty;

    private Integer groupCQty;

    private List<StockGroupAllocationItemDTO> allocations;

    private String outboundMode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime saleDeadline;

    private String remark;
}
