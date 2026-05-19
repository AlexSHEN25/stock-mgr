package co.handk.common.model.dto.create;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockOperateDTO {

    @NotNull(message = "商品は必須項目です")
    private Long goodsId;

    @NotNull(message = "SKUは必須項目です")
    private Long skuId;

    @NotNull(message = "倉庫は必須項目です")
    private Long warehouseId;

    @NotNull(message = "数量は必須項目です")
    @Min(value = 1, message = "数量は1以上である必要があります")
    private Integer quantity;

    private Long stockTypeId;

    /**
     * 1: self inbound (approval required), 2: resale inbound (direct)
     */
    private Integer sourceType;

    private String remark;
}
