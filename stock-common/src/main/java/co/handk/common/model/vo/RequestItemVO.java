package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private BigDecimal discountPrice;
    private String currency;
    private BigDecimal discount;
    private Integer requestQty;
    private Integer approveQty;
    private Integer outQty;
    private BigDecimal totalAmt;
    private BigDecimal depositAmt;
    private LocalDateTime depositTime;
    private BigDecimal depositFee;
    private Long stockRecordId;
    private Integer state;
    private String remark;
}
