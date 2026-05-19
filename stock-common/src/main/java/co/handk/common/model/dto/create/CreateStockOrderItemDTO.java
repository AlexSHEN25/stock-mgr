package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateStockOrderItemDTO {

    @NotNull(message = "伝票IDは必須項目です")
    private Long orderId;
    @NotNull(message = "商品は必須項目です")
    private Long goodsId;
    @NotNull(message = "SKUは必須項目です")
    private Long skuId;
    private String skuCode;
    @NotBlank(message = "商品名は必須項目です")
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
    @NotNull(message = "変更前数量は必須項目です")
    private Integer beforeQty;
    @NotNull(message = "変更数量は必須項目です")
    private Integer changeQty;
    @NotNull(message = "変更後数量は必須項目です")
    private Integer afterQty;
    private BigDecimal price;
    private String currency;
    private String remark;
}

