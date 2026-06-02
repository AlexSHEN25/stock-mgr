package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.DecimalMax;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateRequestItemDTO {
    @NotNull(message = "IDは必須です")
    private Long id;

    @NotNull(message = "申請IDは必須です")
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
    @PositiveOrZero(message = "0以上で入力してください")
    private BigDecimal price;
    @PositiveOrZero(message = "discount price must be zero or greater")
    private BigDecimal discountPrice;
    private String currency;
    @PositiveOrZero(message = "0以上で入力してください")
    @DecimalMax(value = "1.0000", message = "discount must not exceed 1.0000")
    private BigDecimal discount;
    @PositiveOrZero(message = "0以上で入力してください")
    private Integer requestQty;
    @PositiveOrZero(message = "0以上で入力してください")
    private Integer approveQty;
    @PositiveOrZero(message = "0以上で入力してください")
    private Integer outQty;
    @PositiveOrZero(message = "deposit amount must be zero or greater")
    private BigDecimal depositAmt;
    private LocalDateTime depositTime;
    @PositiveOrZero(message = "deposit fee must be zero or greater")
    private BigDecimal depositFee;
    private Long stockRecordId;
    private Integer state;
    private String remark;
}
