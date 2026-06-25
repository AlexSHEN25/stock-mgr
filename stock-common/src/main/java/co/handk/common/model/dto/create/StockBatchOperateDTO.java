package co.handk.common.model.dto.create;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockBatchOperateDTO {

    /**
     * Inbound only:
     * 1: self inbound (approval required)
     * 2: resale inbound (immediate)
     */
    private Integer sourceType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime saleDeadline;

    private String remark;

    @NotEmpty(message = "items is required")
    @Valid
    private List<StockBatchOperateItemDTO> items;
}
