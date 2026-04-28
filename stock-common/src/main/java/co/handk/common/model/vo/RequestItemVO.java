package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RequestItemVO extends BaseVO {
    private Long requestId;
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
    private Long warehouseId;
    private BigDecimal price;
    private String currency;
    private BigDecimal discount;
    private Integer requestQty;
    private Integer approveQty;
    private Integer outQty;
    private Long stockRecordId;
    private String remark;
}

