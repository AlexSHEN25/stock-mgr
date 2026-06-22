package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsListVO {
    private Long id;
    private String name;
    private String englishName;
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
    private String skuStatusDesc;
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
    private Integer sort;
    private Long imageId;
    private String imageUrl;
    private Integer imageSort;
    private Integer status;
    private String statusDesc;
}
