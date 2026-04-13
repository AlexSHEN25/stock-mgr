package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockVO extends BaseVO {
    private Integer goodsId;
    private String goodsName;
    private Long skuId;
    private String sku;
    private Integer typeId;
    private Integer warehouseId;
    private Integer currentQty;
    private Integer lockQty;
    private BigDecimal price;
    private String currency;
    private LocalDateTime priceUpdateTime;
    private Integer status;
    private Long version;
}

