package co.handk.common.model.dto.query;

import co.handk.common.model.PageQuery;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockOrderItemQueryDTO extends PageQuery {

    private Long id;

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
    private Long categoryId;
    private String categoryName;
    private Long stockTypeId;
    private String stockTypeName;
    private Long makerId;
    private String makerName;
    private Integer beforeQty;
    private Integer changeQty;
    private Integer afterQty;
    private BigDecimal price;
    private String currency;
    private String remark;
}

