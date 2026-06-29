package co.handk.common.model.vo;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StockRecordVO extends BaseVO {
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
    private Long categoryId;
    private String categoryName;
    private Long stockTypeId;
    private String stockTypeName;
    private Long makerId;
    private String makerName;
    private Long warehouseId;
    private Integer changeQty;
    private Integer orderType;
    private Integer sourceType;
    private BigDecimal price;
    private String currency;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Tokyo")
    private LocalDateTime priceUpdateTime;
    private Long customerId;
    private String customerName;
    private Long batchId;
    private Long deptId;
    private String deptCode;
    private String outboundMode;
    private Long requesterId;
    private String requesterName;
    private Long operatorId;
    private String operatorName;
    private String remark;
    private LocalDate bizDate;
}
