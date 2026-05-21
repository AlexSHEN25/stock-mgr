package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotNull;
import co.handk.common.enums.StatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private BigDecimal price;

    private String currency;

    private BigDecimal discount;

    private LocalDateTime effectiveTime;

    private LocalDateTime expireTime;

    private StatusEnum status;

}
