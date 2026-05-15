package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberPriceUpsertItemDTO {
    private Long id;

    @NotNull(message = "商品IDは必須です")
    private Long goodsId;

    @NotNull(message = "SKU IDは必須です")
    private Long skuId;

    private String skuCode;

    @NotNull(message = "会員ランクIDは必須です")
    private Long levelId;

    @NotNull(message = "価格は必須です")
    private BigDecimal price;

    private String currency;
    private BigDecimal discount;
    private LocalDateTime effectiveTime;
    private LocalDateTime expireTime;
}

