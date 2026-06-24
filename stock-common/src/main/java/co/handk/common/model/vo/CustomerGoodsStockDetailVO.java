package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CustomerGoodsStockDetailVO {
    private Long recordId;
    private String bizNo;
    private Long orderId;
    private Long orderItemId;
    private String country;
    private Long customerId;
    private String customerName;
    private Long deptId;
    private String groupCode;
    private Long goodsId;
    private String goodsName;
    private Long skuId;
    private String skuCode;
    private Long brandId;
    private String brandName;
    private Long seriesId;
    private String seriesName;
    private Long makerId;
    private String makerName;
    private Long categoryId;
    private String categoryName;
    private Long stockTypeId;
    private String stockTypeName;
    private Integer quantity;
    private BigDecimal price;
    private String currency;
    private LocalDate outboundDate;
    private Long operatorId;
    private String operatorName;
}
