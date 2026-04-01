package co.handk.common.model.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class StockVO {

    private Long id;

    private Integer goodsId;
    private String goodsName;
    private String sku;
    private Integer warehouseId;
    private Integer currentQty;
    private Integer lockQty;
    private BigDecimal price;
    private LocalDateTime priceUpdateTime;
    private Integer status;
    private Long version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
