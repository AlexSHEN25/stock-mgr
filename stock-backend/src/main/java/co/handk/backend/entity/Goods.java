package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class Goods extends BaseEntity {

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
}
