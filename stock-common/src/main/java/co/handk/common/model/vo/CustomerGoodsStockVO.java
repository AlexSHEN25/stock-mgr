package co.handk.common.model.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerGoodsStockVO {
    private Long customerId;
    private String customerName;
    private Long deptId;
    private String groupCode;
    private Long categoryId;
    private String categoryName;
    private Long goodsId;
    private String goodsName;
    private Long skuId;
    private String skuCode;
    private Long stockTypeId;
    private String stockTypeName;
    private Long goodsCount;
    private Long skuCount;
    private Long quantity;
    private Integer status;
    private String statusDesc;
    private String sourceScope;
    private LocalDate lastOutboundDate;
}
