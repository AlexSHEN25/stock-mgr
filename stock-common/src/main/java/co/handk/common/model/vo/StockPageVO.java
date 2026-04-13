package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 蠎灘ｭ伜・鬘ｵ霑泌屓蟇ｹ雎｡
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
     * 螳樣刔蠎灘ｭ・
     */
    private Integer currentQty;

    /**
     * 髞∝ｮ壼ｺ灘ｭ・
     */
    private Integer lockQty;

    /**
     * 蜿ｯ逕ｨ蠎灘ｭ・= 螳樣刔蠎灘ｭ・- 髞∝ｮ壼ｺ灘ｭ・
     */
    private Integer availableQty;

    private BigDecimal price;
    private String currency;

    private LocalDateTime priceUpdateTime;

    private Integer status;
}

