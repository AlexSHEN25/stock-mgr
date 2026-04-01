package co.handk.common.model.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class GoodsVO {

    private Long id;

    private String name;
    private String englishName;
    private String sku;
    private Long seriesId;
    private Long brandId;
    private Long typeId;
    private Long makerId;
    private BigDecimal price;
    private BigDecimal discount;
    private Integer status;
    private BigDecimal newPrice;
    private LocalDateTime priceUpdateTime;
    private String images;
    private String description;
    private Integer isHot;
    private Long version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
