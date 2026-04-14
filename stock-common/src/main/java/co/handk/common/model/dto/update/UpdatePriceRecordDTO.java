package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UpdatePriceRecordDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private Long goodsId;
    private String goodsName;
    private String englishName;
    private Long skuId;
    private String skuCode;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private String currency;
    private BigDecimal discount;
    private LocalDateTime priceUpdateTime;
    private Long operatorId;
    private String operatorName;
}

