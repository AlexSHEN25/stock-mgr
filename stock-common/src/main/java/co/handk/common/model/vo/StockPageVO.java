package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存分页返回对象
 */
@Data
public class StockPageVO {

    private Long id;

    private Long goodsId;

    private String goodsName;

    private String sku;

    private Long warehouseId;

    /**
     * 实际库存
     */
    private Integer currentQty;

    /**
     * 锁定库存
     */
    private Integer lockQty;

    /**
     * 可用库存 = 实际库存 - 锁定库存
     */
    private Integer availableQty;

    private BigDecimal price;

    private LocalDateTime priceUpdateTime;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}