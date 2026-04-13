package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class StockOrderItem extends BaseEntity {

    private Long orderId;

    private Long goodsId;

    private Long skuId;

    private String skuCode;

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

