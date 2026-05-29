package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class UpdateStockOrderItemDTO {

    @NotNull(message = "IDは必須です")
    private Long id;
    @NotNull(message = "注文IDは必須です")
    private Long orderId;
    @NotNull(message = "商品IDは必須です")
    private Long goodsId;
    @NotNull(message = "SKU IDは必須です")
    private Long skuId;
    private String skuCode;
    @NotBlank(message = "商品名は必須です")
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
    @NotNull(message = "変更前数量は必須です")
    @PositiveOrZero(message = "0以上で入力してください")
    private Integer beforeQty;
    @NotNull(message = "変更数量は必須です")
    @PositiveOrZero(message = "0以上で入力してください")
    private Integer changeQty;
    @NotNull(message = "変更後数量は必須です")
    @PositiveOrZero(message = "0以上で入力してください")
    private Integer afterQty;
    @PositiveOrZero(message = "0以上で入力してください")
    private BigDecimal price;
    private String currency;
    private String remark;
    private LocalDateTime bizDate;
}
