package co.handk.common.model.dto.query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import co.handk.common.model.PageQuery;

@Data
public class PriceRecordQueryDTO extends PageQuery {

    private Long id;

    private Long goodsId;
    private String goodsName;
    private String englishName;
    private Long skuId;
    private String sku;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private String currency;
    private BigDecimal discount;
    private LocalDateTime priceUpdateTime;
    private Long operatorId;
    private String operatorName;
}

