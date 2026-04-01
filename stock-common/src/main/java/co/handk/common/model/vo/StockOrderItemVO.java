package co.handk.common.model.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class StockOrderItemVO {

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
