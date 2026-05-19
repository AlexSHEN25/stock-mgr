package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsVO extends BaseVO {
    private String name;
    private String englishName;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal price;
    private BigDecimal costPrice;
    private BigDecimal updatePrice;
    private LocalDateTime priceUpdateTime;
    private String currency;
    private Long brandId;
    private String brandName;
    private Long seriesId;
    private String seriesName;
    private Long categoryId;
    private String categoryName;
    private Long makerId;
    private String makerName;
    private String description;
    private Integer isHot;
    private Long imageId;
    private String imageUrl;
    private String barcode;
    private BigDecimal weight;
    private BigDecimal volume;
    private Integer sort;
    private Integer status;
    private String statusDesc;
}
