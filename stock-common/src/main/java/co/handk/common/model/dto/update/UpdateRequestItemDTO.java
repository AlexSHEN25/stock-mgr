package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class UpdateRequestItemDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

    @NotNull(message = "申請IDは必須項目です")
    private Long requestId;
    @NotNull(message = "商品は必須項目です")
    private Long goodsId;
    @NotNull(message = "SKUは必須項目です")
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
    private String remark;
}

