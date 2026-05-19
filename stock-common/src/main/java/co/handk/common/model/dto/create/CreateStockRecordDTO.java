package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateStockRecordDTO {

    @NotBlank(message = "業務番号は必須項目です")
    private String bizNo;
    @NotNull(message = "伝票IDは必須項目です")
    private Long orderId;
    @NotNull(message = "伝票明細IDは必須項目です")
    private Long orderItemId;
    @NotNull(message = "在庫IDは必須項目です")
    private Long stockId;
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
    private Long warehouseId;
    @NotNull(message = "変更前数量は必須項目です")
    private Integer beforeQty;
    @NotNull(message = "変更数量は必須項目です")
    private Integer changeQty;
    @NotNull(message = "変更後数量は必須項目です")
    private Integer afterQty;
    @NotNull(message = "入出庫種別は必須項目です")
    private Integer orderType;
    @NotNull(message = "入出庫ソースは必須項目です")
    private Integer sourceType;
    private BigDecimal price;
    private String currency;
    private LocalDateTime priceUpdateTime;
    private Long customerId;
    private String customerName;
    private Long requesterId;
    private String requesterName;
    private Long operatorId;
    private String operatorName;
    private String remark;
}

