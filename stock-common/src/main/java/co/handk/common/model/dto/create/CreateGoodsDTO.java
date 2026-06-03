package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateGoodsDTO {

    @NotBlank(message = "名称は必須項目です")
    private String name;
    private String englishName;
    @NotNull(message = "ブランドは必須項目です")
    private Long brandId;
    private Long seriesId;
    @NotNull(message = "カテゴリーは必須項目です")
    private Long categoryId;
    private Long makerId;
    private String description;
    private Integer isHot;
    private Integer sort;
    private Long skuId;
    private String skuCode;
    private String skuName;
    @PositiveOrZero(message = "0以上で入力してください")
    private BigDecimal price;
    private String currency;
    @PositiveOrZero(message = "0以上で入力してください")
    private BigDecimal costPrice;
    @PositiveOrZero(message = "0以上で入力してください")
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
