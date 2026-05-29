package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsListVO extends BaseVO {
    private String name;
    private String englishName;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal price;
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
    private Integer sort;
    private Integer status;
    private String statusDesc;
}
