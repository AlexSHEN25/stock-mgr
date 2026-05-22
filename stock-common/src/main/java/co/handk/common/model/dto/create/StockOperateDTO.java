package co.handk.common.model.dto.create;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockOperateDTO {

    @NotNull(message = "在庫商品IDは必須です")
    private Long stockId;

    @NotNull(message = "数量は必須です")
    @Min(value = 1, message = "数量は1以上で入力してください")
    private Integer quantity;

    /**
     * 入庫のみ有効
     * 1: 自社入庫（承認必要）
     * 2: 再販入庫（即時反映）
     */
    private Integer sourceType;

    private String remark;
}
