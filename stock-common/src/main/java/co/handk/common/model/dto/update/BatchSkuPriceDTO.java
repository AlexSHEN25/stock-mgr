package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BatchSkuPriceDTO {
    @NotEmpty(message = "SKU ID一覧は必須です")
    private List<Long> skuIds;

    @NotNull(message = "価格は必須です")
    private BigDecimal price;

    @NotBlank(message = "通貨は必須です")
    private String currency;
}

