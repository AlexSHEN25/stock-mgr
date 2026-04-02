package co.handk.common.model.dto.query;

import java.math.BigDecimal;
import lombok.Data;
import co.handk.common.model.PageQuery;

@Data
public class StockOrderItemQueryDTO extends PageQuery {

    private Long id;

    private Long orderId;
    private Long goodsId;
    private String sku;
    private String goodsName;
    private String englishName;
    private Long brandId;
    private String brandName;
    private Long seriesId;
    private String seriesName;
    private Long typeId;
    private String typeName;
    private Long makerId;
    private String makerName;
    private Integer beforeQty;
    private Integer changeQty;
    private Integer afterQty;
    private BigDecimal price;
    private String remark;
}
