package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageQuery;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockQueryDTO extends PageQuery {

    private Long goodsId;
    private String goodsName;
    private String skuCode;
    private Long skuId;
    private Long stockTypeId;
    private Integer currentQty;
    private Integer lockQty;
    private BigDecimal price;
    private LocalDateTime priceUpdateTime;
    private String currency;
    private Long warehouseId;
    private StatusEnum status;
}

