package co.handk.common.model.dto.create;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockBatchOperateItemDTO {

    private Long stockId;

    private Integer goodsId;

    private Long skuId;

    private Integer warehouseId;

    private Long stockTypeId;

    @JsonAlias({"outQty", "outboundQty", "outboundQuantity", "splitQty", "customerQty", "deliveryQty", "qty"})
    @NotNull(message = "数量は必須です")
    @Min(value = 1, message = "数量は1以上で入力してください")
    private Integer quantity;

    private String remark;
}
