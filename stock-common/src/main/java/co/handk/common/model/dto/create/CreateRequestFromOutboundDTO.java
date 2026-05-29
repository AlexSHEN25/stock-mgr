package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateRequestFromOutboundDTO {

    @NotNull(message = "出庫伝票IDは必須です")
    private Long stockOrderId;

    /**
     * Optional: when empty, all outbound items under stockOrderId will be used.
     */
    private List<Long> stockOrderItemIds;

    private String remark;
}

