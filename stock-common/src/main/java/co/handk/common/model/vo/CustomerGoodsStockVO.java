package co.handk.common.model.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerGoodsStockVO {
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
    private Long quantity;
    private LocalDate lastOutboundDate;
}
