package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RequestCandidateItemVO {
    private Long stockOrderId;
    private Long stockOrderItemId;
    private String orderNo;
    private Integer orderType;
    private LocalDate bizDate;
    private Long goodsId;
    private Long skuId;
    private String skuCode;
    private String goodsName;
    private String brandName;
    private String seriesName;
    private String categoryName;
    private String stockTypeName;
    private String makerName;
    private Integer changeQty;
    private BigDecimal price;
    private String currency;
    private Boolean selected;
    private Integer requestItemState;
    private Long requestItemId;
}

