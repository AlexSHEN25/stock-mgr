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
    @NotNull(message = "明細は必須です")
    private List<Item> items = new ArrayList<>();

    @Data
    public static class Item {

        private Long id;

        private Long requestId;

        @NotNull(message = "商品IDは必須です")
        private Long goodsId;

        @NotNull(message = "SKU IDは必須です")
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

        @PositiveOrZero(message = "単価は0以上で入力してください")
        private BigDecimal price;

        @PositiveOrZero(message = "割引後単価は0以上で入力してください")
        private BigDecimal discountPrice;

        private BigDecimal exchangeRate;

        private String currency;

        @PositiveOrZero(message = "割引率は0以上で入力してください")
        @DecimalMax(value = "1.0000", message = "割引率は1.0000以下で入力してください")
        private BigDecimal discount;

        @PositiveOrZero(message = "申請数量は0以上で入力してください")
        private Integer requestQty;

        @PositiveOrZero(message = "承認数量は0以上で入力してください")
        private Integer approveQty;

        @PositiveOrZero(message = "出庫数量は0以上で入力してください")
        private Integer outQty;

        @PositiveOrZero(message = "入金額は0以上で入力してください")
        private BigDecimal depositAmt;

        private LocalDateTime depositTime;

        @PositiveOrZero(message = "入金手数料は0以上で入力してください")
        private BigDecimal depositFee;

        private Long stockRecordId;

        private Integer state;

        private String remark;
    }
}
