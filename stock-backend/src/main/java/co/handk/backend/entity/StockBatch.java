package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class StockBatch extends BaseEntity {
    private Long inboundOrderId;
    private Long inboundOrderItemId;
    private Long stockId;
    private Long goodsId;
    private Long skuId;
    private Long warehouseId;
    private Long stockTypeId;
    private Integer originalQty;
    private Integer currentQty;
    private Integer availableQty;
    private Integer allocatedQty;
    private Integer customerOutQty;
    private LocalDateTime saleDeadline;
    private Integer state;
    private Long version;
}
