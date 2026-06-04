package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class RequestCandidateItemVO {
    private Long stockRecordId;
    private Long stockOrderId;
    private Long stockOrderItemId;
    private String orderNo;
    private Integer orderType;
    private LocalDate bizDate;
    private Long goodsId;
    private Long skuId;
    private String skuCode;
    private String goodsName;
    private Long brandId;
    private String brandName;
    private Long seriesId;
    private String seriesName;
    private Long makerId;
    private String categoryName;
    private String stockTypeName;
    private String makerName;
    private Integer changeQty;
    private BigDecimal price;
    private String currency;
    private Boolean selected;
    private Integer requestQty;
    private Integer requestItemState;
    private Long requestItemId;
    private Boolean knife;
    private Boolean handle;
    private List<RequestCandidateItemVO> handleCandidates;
}
