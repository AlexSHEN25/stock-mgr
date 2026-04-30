package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageQuery;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsQueryDTO extends PageQuery {

    private Long id;

    private String name;
    private String englishName;
    private String skuCode;
    private Long seriesId;
    private Long brandId;
    private Long categoryId;
    private Long makerId;
    private BigDecimal price;
    private BigDecimal discount;
    private StatusEnum status;
    private BigDecimal newPrice;
    private LocalDateTime priceUpdateTime;
    private String images;
    private String description;
    private Integer isHot;
    private Long version;
}
