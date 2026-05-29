package co.handk.common.model.dto.create;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockOrderSubmitItemDTO {

    @NotNull(message = "在庫IDは必須です")
    private Long stockId;

    @NotNull(message = "数量は必須です")
    @Min(value = 1, message = "数量は1以上で入力してください")
    private Integer quantity;

    private String remark;
}
