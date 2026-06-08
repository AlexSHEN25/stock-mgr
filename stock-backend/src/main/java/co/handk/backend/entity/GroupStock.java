package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class GroupStock extends BaseEntity {
    private Long batchId;
    private Long deptId;
    private String deptCode;
    private Long stockId;
    private Long goodsId;
    private Long skuId;
    private Long warehouseId;
    private Long stockTypeId;
    private Integer allocatedQty;
    private Integer currentQty;
    private LocalDateTime saleDeadline;
    private Integer state;
    private Long version;
}
