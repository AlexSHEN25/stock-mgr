package co.handk.common.model.dto.update;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class RequestCartMoveDTO {
    private Long customerId;

    @Valid
    @NotEmpty(message = "items is required")
    private List<Item> items;

    private String remark;

    @Data
    public static class Item {
        private Long stockRecordId;

        private List<Long> stockRecordIds;

        private Long stockOrderItemId;

        private List<Long> stockOrderItemIds;

        @NotNull(message = "requestQty is required")
        @Positive(message = "requestQty must be greater than zero")
        private Integer requestQty;
    }
}
