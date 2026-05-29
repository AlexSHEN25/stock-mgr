package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotNull;
import co.handk.common.enums.StatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateGoodsLevelPriceDTO {

    @NotNull(message = "必須項目です")
    private Long goodsId;

    @NotNull(message = "必須項目です")
    private Long skuId;

    private String skuCode;

    @NotNull(message = "必須項目です")
    private Long levelId;

    @NotNull(message = "必須項目です")
    @PositiveOrZero(message = "0以上で入力してください")
    private BigDecimal price;

    private String currency;

    @PositiveOrZero(message = "0以上で入力してください")
    private BigDecimal discount;

    private LocalDateTime effectiveTime;

    private LocalDateTime expireTime;

    private StatusEnum status;

}
