package co.handk.common.model.dto.query;

import co.handk.common.model.PageQuery;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsQueryDTO extends PageQuery {

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
    private Integer isHot;
    private Integer sort;
    private Integer status;
    private String statusDesc;
}
