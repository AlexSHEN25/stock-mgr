package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateGoodsLevelPriceDTO {
    @NotNull(message = "ID闕ｳ蟠弱・闕ｳ・ｺ驕ｨ・ｺ")
    private Long id;

    private Long goodsId;

    private Long skuId;

    private String skuCode;

    private Long levelId;

    private BigDecimal price;
    private String currency;

    private BigDecimal discount;

    private LocalDateTime effectiveTime;

    private LocalDateTime expireTime;

    private StatusEnum status;
}

