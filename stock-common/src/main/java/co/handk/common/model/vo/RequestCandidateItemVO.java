package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class RequestCandidateItemVO {
    private Long stockRecordId;
    private List<Long> stockRecordIds;
    private Long stockOrderId;
    private Long stockOrderItemId;
    private List<Long> stockOrderItemIds;
    private String orderNo;
    private Integer orderType;
    private Integer state;
    private LocalDate bizDate;
    private String country;
    private String groupCode;
    private Long customerId;
    private String customerName;
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
    private Integer availableQty;
    private BigDecimal price;
    private String currency;
    private String requesterName;
    private String operatorName;
    private Boolean selected;
    private Integer requestQty;
    private Integer requestItemState;
    private Long requestItemId;
    private Boolean inboundApplied;
    private Long inboundOrderId;
    private Boolean knife;
    private Boolean handle;
    private List<RequestCandidateItemVO> handleCandidates;
}
