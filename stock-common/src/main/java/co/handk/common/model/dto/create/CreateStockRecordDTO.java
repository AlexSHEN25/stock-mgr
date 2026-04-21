package co.handk.common.model.dto.create;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateStockRecordDTO {

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
    private Integer type;
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

