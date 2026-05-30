package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
        @NotNull(message = "stockRecordIdは必須です")
        private Long stockRecordId;

        @NotNull(message = "requestQtyは必須です")
        @PositiveOrZero(message = "requestQtyは0以上で入力してください")
        private Integer requestQty;
    }
}
