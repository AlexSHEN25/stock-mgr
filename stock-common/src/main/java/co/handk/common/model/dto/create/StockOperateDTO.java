package co.handk.common.model.dto.create;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockOperateDTO {

    private Long stockId;

    private Integer goodsId;

    private Long skuId;

    private Integer warehouseId;

    private Long stockTypeId;

    private Long batchId;

    @JsonAlias({"outQty", "outboundQty", "outboundQuantity", "splitQty", "customerQty", "deliveryQty", "qty"})
    private Integer quantity;

    /**
     * Inbound only:
     * 1: self inbound (approval required)
     * 2: resale inbound (immediate)
     */
    private Integer sourceType;

    private Long customerId;

    private String customerName;

    private Long deptId;

    private String groupCode;

    private String deptCode;

    private Integer groupAQty;

    private Integer groupBQty;

    private Integer groupCQty;

    private List<StockGroupAllocationItemDTO> allocations;

    @JsonAlias({"customerItems", "customers", "customerRows", "customerList", "splits", "items"})
    private List<StockCustomerOutboundItemDTO> customerItems;

    private String outboundMode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Tokyo")
    private LocalDateTime saleDeadline;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Tokyo")
    private LocalDate bizDate;

    private String remark;
}
