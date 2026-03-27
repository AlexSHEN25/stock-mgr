package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class RequestItem extends BaseEntity {

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
}
