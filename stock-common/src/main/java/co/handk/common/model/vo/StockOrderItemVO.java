package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockOrderItemVO extends BaseVO {
    private Long orderId;
    private Long goodsId;
    private Long skuId;
    private String sku;
    private String goodsName;
    private String englishName;
    private Long brandId;
    private String brandName;
    private Long seriesId;
    private String seriesName;
    private Long typeId;
    private String typeName;
    private Long makerId;
    private String makerName;
    private Integer beforeQty;
    private Integer changeQty;
    private Integer afterQty;
    private BigDecimal price;
    private String currency;
    private String remark;
}

