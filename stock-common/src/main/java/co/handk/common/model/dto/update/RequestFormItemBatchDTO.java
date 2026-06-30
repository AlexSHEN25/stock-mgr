package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.List;

@Data
public class RequestFormItemBatchDTO {

    @NotNull(message = "請求書IDは必須です")
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

        private List<Long> stockRecordIds;

        private Long stockOrderItemId;

        private List<Long> stockOrderItemIds;

        /**
         * Optional matched handle stock record selected for a knife row.
         */
        private Long handleStockRecordId;

        /**
         * Optional matched handle stock records when one knife row needs multiple handles.
         */
        private List<Long> handleStockRecordIds;

        @NotNull(message = "請求数量は必須です")
        @PositiveOrZero(message = "requestQty must be zero or greater")
        private Integer requestQty;
    }
}
