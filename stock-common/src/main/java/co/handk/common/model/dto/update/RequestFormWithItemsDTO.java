package co.handk.common.model.dto.update;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class RequestFormWithItemsDTO {

    private Long id;

    private String bizNo;

    private Long customerId;

    private String customerName;

    private Long warehouseId;

    private Long sourceOrderId;

    private Integer state;

    private Long approverId;

    private String approverName;

    private LocalDateTime approveTime;

    private String approveRemark;

    @Valid
    @NotNull(message = "items are required")
    private List<Item> items = new ArrayList<>();

    @Data
    public static class Item {

        private Long id;

        private Long requestId;

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

        @PositiveOrZero(message = "price must be zero or greater")
        private BigDecimal price;

        @PositiveOrZero(message = "discount price must be zero or greater")
        private BigDecimal discountPrice;

        private BigDecimal exchangeRate;

        private String currency;

        @PositiveOrZero(message = "discount must be zero or greater")
        @DecimalMax(value = "1.0000", message = "discount must be 1.0000 or less")
        private BigDecimal discount;

        @PositiveOrZero(message = "request qty must be zero or greater")
        private Integer requestQty;

        @PositiveOrZero(message = "approve qty must be zero or greater")
        private Integer approveQty;

        @PositiveOrZero(message = "out qty must be zero or greater")
        private Integer outQty;

        @PositiveOrZero(message = "deposit amount must be zero or greater")
        private BigDecimal depositAmt;

        private LocalDateTime depositTime;

        @PositiveOrZero(message = "deposit fee must be zero or greater")
        private BigDecimal depositFee;

        @PositiveOrZero(message = "unpaid amount must be zero or greater")
        private BigDecimal unpaidAmt;

        private Long stockRecordId;

        private List<Long> stockRecordIds;

        private Long stockOrderItemId;

        private List<Long> stockOrderItemIds;

        private Integer state;

        private String remark;
    }
}
