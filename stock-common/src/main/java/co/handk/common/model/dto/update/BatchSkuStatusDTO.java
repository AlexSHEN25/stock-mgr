package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BatchSkuStatusDTO {
    @NotEmpty(message = "SKU ID一覧は必須です")
    private List<Long> skuIds;

    @NotNull(message = "状態は必須です")
    private StatusEnum status;
}

