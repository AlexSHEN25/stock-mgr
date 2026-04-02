package co.handk.common.model.dto.create;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CreatePriceRecordDTO {

    private Long goodsId;
    private String goodsName;
    private String englishName;
    private String sku;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private BigDecimal discount;
    private LocalDateTime priceUpdateTime;
    private Long operatorId;
    private String operatorName;
}
