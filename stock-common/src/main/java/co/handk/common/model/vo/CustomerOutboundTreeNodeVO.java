package co.handk.common.model.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CustomerOutboundTreeNodeVO {
    private String id;
    private String nodeType;
    private String country;
    private Long customerId;
    private String customerName;
    private Long deptId;
    private String groupCode;
    private Integer totalQuantity;
    private Integer goodsKinds;
    private String bizNo;
    private Long orderId;
    private Long orderItemId;
    private Long recordId;
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
    private List<CustomerOutboundTreeNodeVO> children;
}
