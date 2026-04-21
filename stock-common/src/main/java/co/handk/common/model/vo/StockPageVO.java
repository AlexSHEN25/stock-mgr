package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存分页展示对象
 */
@Data
public class StockPageVO extends BaseVO {
    private Integer goodsId;

    private String goodsName;

    private Long skuId;

    private String skuCode;

    private Integer typeId;

    private Integer warehouseId;

    /**
     * 当前库存
     */
    private Integer currentQty;

    /**
     * 锁定库存
     */
    private Integer lockQty;

    /**
     * 可用库存 = 当前库存 - 锁定库存
     */
    private Integer availableQty;

    private BigDecimal price;
    private String currency;

    private LocalDateTime priceUpdateTime;

    private Integer status;
}

