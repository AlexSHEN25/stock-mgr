package co.handk.common.model.dto.update;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MemberPriceBatchUpsertDTO {
    @Valid
    @NotEmpty(message = "会員価格データは必須です")
    private List<MemberPriceUpsertItemDTO> items;
}

