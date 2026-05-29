package co.handk.common.model.vo;

import co.handk.common.annotation.JoinValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockVO extends BaseVO {
    private Integer goodsId;
    private String goodsName;
    private Long skuId;
    private String skuCode;
    private Integer warehouseId;
    private Integer currentQty;
    private Integer lockQty;
    private BigDecimal price;
    private String currency;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime priceUpdateTime;
    private Long stockTypeId;
    @JoinValue(sourceField = "stockTypeId", serviceBean = "stockTypeServiceImpl", targetField = "name")
    private String stockTypeName;
    private Integer status;
    private String statusDesc;
}
