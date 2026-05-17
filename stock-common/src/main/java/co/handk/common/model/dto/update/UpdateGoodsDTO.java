package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateGoodsDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private String name;
    private String englishName;
    private Long seriesId;
    private Long brandId;
    private Long categoryId;
    private Long makerId;
    private String description;
    private Integer isHot;
    private Integer sort;
    private StatusEnum status;

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
}
