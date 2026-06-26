package co.handk.common.model.dto.goods;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsBatchUpsertItemDTO {

    private Integer rowNo;
    private Long goodsId;
    private Long skuId;
    private String name;
    private String englishName;
    private Long brandId;
    private String brandName;
    private String brandEnglishName;
    private Long seriesId;
    private String seriesName;
    private String seriesEnglishName;
    private Long categoryId;
    private String categoryName;
    private Long makerId;
    private String makerName;
    private String makerEnglishName;
    private String description;
    private String isHot;
    private Integer sort;
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
    private String skuStatus;
    private Long imageId;
    private String imageUrl;
    private Integer imageSort;
    private String status;
}
