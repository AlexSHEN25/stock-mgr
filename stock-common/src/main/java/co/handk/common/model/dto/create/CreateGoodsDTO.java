package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateGoodsDTO {

    private String name;
    private String englishName;
    private Long brandId;
    private Long seriesId;
    private Long categoryId;
    private Long makerId;
    private String description;
    private Integer isHot;
    private Integer sort;

    @NotBlank(message = "SKUコードは必須です")
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

    @NotBlank(message = "商品画像URLは必須です")
    private String imageUrl;
    private Integer imageSort;
}
