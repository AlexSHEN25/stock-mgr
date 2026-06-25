package co.handk.common.model.dto.create;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockBatchOperateItemDTO {

    private Long stockId;

    private Integer goodsId;

    private Long skuId;

    private Integer warehouseId;

    private Long stockTypeId;

    /**
     * Inbound only. Falls back to the batch-level source type when omitted.
     */
    private Integer sourceType;

    @JsonAlias({"outQty", "outboundQty", "outboundQuantity", "splitQty", "customerQty", "deliveryQty", "qty"})
    @NotNull(message = "数量は必須です")
    @Min(value = 1, message = "数量は1以上で入力してください")
    private Integer quantity;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime saleDeadline;

    private String remark;
}
