package co.handk.common.model.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RequestItemVO {

    private Long id;

    private Long requestId;
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
    private Long warehouseId;
    private BigDecimal price;
    private BigDecimal discount;
    private Integer requestQty;
    private Integer approveQty;
    private Integer outQty;
    private Long stockRecordId;
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
