package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateGoodsDTO {

    @NotBlank(message = "必須項目です")
    private String name;
    private String englishName;
    @NotNull(message = "必須項目です")
    private Long brandId;
    private Long seriesId;
    @NotNull(message = "必須項目です")
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
