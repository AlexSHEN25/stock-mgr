package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class StockReservation extends BaseEntity {
    private String idempotencyKey;
    private Long orderId;
    private Long orderItemId;
    private Long stockId;
    private Long batchId;
    private Long groupStockId;
    private Long goodsId;
    private Long skuId;
    private Long warehouseId;
    private Long stockTypeId;
    private String reservationScope;
    private String outboundMode;
    private Long deptId;
    private String deptCode;
    private Long customerId;
    private String customerName;
    private Integer reservationQty;
    private Integer state;
    private LocalDateTime confirmTime;
    private LocalDateTime releaseTime;
    private LocalDateTime expiresAt;
}
