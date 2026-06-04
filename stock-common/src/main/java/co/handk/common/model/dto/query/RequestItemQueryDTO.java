package co.handk.common.model.dto.query;

import co.handk.common.model.PageQuery;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RequestItemQueryDTO extends PageQuery {


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
    private Long makerId;
    private String makerName;
    private Long stockTypeId;
    private String stockTypeName;
    private Long warehouseId;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal exchangeRate;
    private String currency;
    private BigDecimal discount;
    private Integer requestQty;
    private Integer approveQty;
    private Integer outQty;
    private BigDecimal totalAmt;
    private BigDecimal depositAmt;
    private LocalDateTime depositTime;
    private BigDecimal depositFee;
    private BigDecimal unpaidAmt;
    private Long stockRecordId;
    private String remark;
}

