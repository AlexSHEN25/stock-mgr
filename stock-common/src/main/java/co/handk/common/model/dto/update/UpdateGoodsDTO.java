package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateGoodsDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

    @NotBlank(message = "名称は必須項目です")
    private String name;
    private String englishName;
    @NotNull(message = "ブランドは必須項目です")
    private Long brandId;
    @NotNull(message = "シリーズは必須項目です")
    private Long seriesId;
    @NotNull(message = "カテゴリは必須項目です")
    private Long categoryId;
    @NotNull(message = "メーカーは必須項目です")
    private Long makerId;
    private String description;
    private Integer isHot;
    private Integer sort;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private BigDecimal price;
    private String currency;
    private BigDecimal costPrice;
    private BigDecimal updatePrice;
    private LocalDateTime priceUpdateTime;
    private String barcode;
    private BigDecimal weight;
    private BigDecimal volume;
    private StatusEnum skuStatus;
    private Long imageId;
    private String imageUrl;
    private Integer imageSort;
    private StatusEnum status;

}
