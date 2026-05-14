package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsBundleVO extends BaseVO {

    private Long goodsId;
    private String goodsName;
    private String goodsEnglishName;
    private Long brandId;
    private String brandName;
    private Long seriesId;
    private String seriesName;
    private Long categoryId;
    private String categoryName;
    private Long makerId;
    private String makerName;
    private Integer isHot;
    private Integer goodsSort;
    private Integer goodsStatus;

    private Long skuId;
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
    private Integer skuStatus;

    private Long specId;
    private String specName;
    private String specValue;
    private Integer specSort;

    private Long imageId;
    private String imageUrl;
    private Integer imageSort;
}

