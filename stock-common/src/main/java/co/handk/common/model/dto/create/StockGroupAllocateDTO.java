package co.handk.common.model.dto.create;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockGroupAllocateDTO {

    private Long stockId;

    private Integer goodsId;

    private Long skuId;

    private Integer warehouseId;

    private Long stockTypeId;

    @Valid
    @NotEmpty(message = "allocations are required")
    private List<StockGroupAllocationItemDTO> allocations;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Tokyo")
    private LocalDateTime saleDeadline;

    private String remark;
}
