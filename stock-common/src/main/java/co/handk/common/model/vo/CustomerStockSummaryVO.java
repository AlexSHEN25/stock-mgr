package co.handk.common.model.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerStockSummaryVO {
    private Long customerId;
    private String customerName;
    private Long deptId;
    private String groupCode;
    private Long goodsKinds;
    private Long totalQuantity;
    private LocalDate lastOutboundDate;
}
