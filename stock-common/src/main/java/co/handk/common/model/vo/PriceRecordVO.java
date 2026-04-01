package co.handk.common.model.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PriceRecordVO {

    private Long id;

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
