package co.handk.common.model.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerGoodsStockDetailVO {
    private Long recordId;
    private String bizNo;
    private Long orderId;
    private Long orderItemId;
    private Long customerId;
    private String customerName;
    private Long deptId;
    private String groupCode;
    private Long goodsId;
    private String goodsName;
    private Long skuId;
    private String skuCode;
    private Long stockTypeId;
    private String stockTypeName;
    private Integer quantity;
    private LocalDate outboundDate;
    private Long operatorId;
    private String operatorName;
}
