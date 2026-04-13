package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UpdateStockRecordDTO {
    @NotNull(message = "ID荳崎・荳ｺ遨ｺ")
    private Long id;

    private String bizNo;
    private Long orderId;
    private Long orderItemId;
    private Long stockId;
    private Long goodsId;
    private Long skuId;
    private String sku;
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

