package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PriceRecordVO extends BaseVO {
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
