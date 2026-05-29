package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRequestItemDTO {

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
    private String currency;
    @PositiveOrZero(message = "0以上で入力してください")
    private BigDecimal discount;
    @PositiveOrZero(message = "0以上で入力してください")
    private Integer requestQty;
    @PositiveOrZero(message = "0以上で入力してください")
    private Integer approveQty;
    @PositiveOrZero(message = "0以上で入力してください")
    private Integer outQty;
    private Long stockRecordId;
    private Integer state;
    private String remark;
}
