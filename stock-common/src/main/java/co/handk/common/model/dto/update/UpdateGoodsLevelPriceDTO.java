package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class UpdateGoodsLevelPriceDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

    @NotNull(message = "商品は必須項目です")
    private Long goodsId;

    @NotNull(message = "SKUは必須項目です")
    private Long skuId;

    private String skuCode;

    @NotNull(message = "会員ランクは必須項目です")
    private Long levelId;

    @NotNull(message = "価格は必須項目です")
    @PositiveOrZero(message = "0以上で入力してください")
    private BigDecimal price;
    private String currency;

    @PositiveOrZero(message = "0以上で入力してください")
    private BigDecimal discount;

    private LocalDateTime effectiveTime;

    private LocalDateTime expireTime;

    private StatusEnum status;
}

