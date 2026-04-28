package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsSkuVO extends BaseVO {
    private Long goodsId;
    private String skuCode;
    private String skuName;
    private BigDecimal price;
    private String currency;
    private BigDecimal costPrice;
    private BigDecimal updatePrice;
    private LocalDateTime priceUpdateTime;
    private String barcode;
    private BigDecimal weight;
    private BigDecimal volume;
    private Integer status;
    private String statusDesc;
}

