package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

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
    private BigDecimal price;
    private String currency;
    private BigDecimal discount;
    private Integer requestQty;
    private Integer approveQty;
    private Integer outQty;
    private Long stockRecordId;
    private String remark;
}

