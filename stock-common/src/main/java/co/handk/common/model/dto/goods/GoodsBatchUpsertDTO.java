package co.handk.common.model.dto.goods;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class GoodsBatchUpsertDTO {

    @NotEmpty(message = "明細は必須です")
    @Valid
    private List<GoodsBatchUpsertItemDTO> items;
}
