package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class StockRecord extends BaseEntity {

    private String bizNo;

    private Long orderId;

    private Long orderItemId;

    private Long stockId;

    private Long goodsId;

    private Long skuId;

    private String skuCode;

    private String goodsName;

    private String englishName;

    private Long brandId;

    private String brandName;

    private Long seriesId;

    private String seriesName;

    private Long typeId;

    private String typeName;

    private Long makerId;

    private String makerName;

    private Long warehouseId;

    private Integer beforeQty;

    private Integer changeQty;

    private Integer afterQty;

    private Integer orderType;

    private Integer sourceType;

    private BigDecimal price;
    private String currency;

    private LocalDateTime priceUpdateTime;

    private Long customerId;

    private String customerName;

    private Long requesterId;

    private String requesterName;

    private Long operatorId;

    private String operatorName;

    private String remark;
}

