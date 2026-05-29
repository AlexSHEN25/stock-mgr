package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RequestFormItemBatchDTO {

    @NotNull(message = "requestId is required")
    private Long requestId;

    /**
     * Backward compatible field name. Values are stock_record.id.
     */
    private List<Long> stockOrderItemIds;

    /**
     * Preferred payload when caller needs to choose a partial quantity.
     */
    private List<Item> items;

    private String remark;

    @Data
    public static class Item {
        private Long stockRecordId;
        private Integer requestQty;
    }
}
