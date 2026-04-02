package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UpdateGoodsDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private String name;
    private String englishName;
    private String sku;
    private Long seriesId;
    private Long brandId;
    private Long typeId;
    private Long makerId;
    private BigDecimal price;
    private BigDecimal discount;
    private StatusEnum status;
    private BigDecimal newPrice;
    private LocalDateTime priceUpdateTime;
    private String images;
    private String description;
    private Integer isHot;
    private Long version;
}
