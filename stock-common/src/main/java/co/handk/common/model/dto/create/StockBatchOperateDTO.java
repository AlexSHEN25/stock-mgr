package co.handk.common.model.dto.create;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class StockBatchOperateDTO {

    /**
     * Inbound only:
     * 1: self inbound (approval required)
     * 2: resale inbound (immediate)
     */
    private Integer sourceType;

    private String remark;

    @NotEmpty(message = "items is required")
    @Valid
    private List<StockBatchOperateItemDTO> items;
}
