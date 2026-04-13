package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class UpdateStockOrderItemDTO {
    @NotNull(message = "ID荳崎・荳ｺ遨ｺ")
    private Long id;

    private Long orderId;
    private Long goodsId;
    private Long skuId;
    private String sku;
    private String goodsName;
    private String englishName;
    private Long brandId;
    private String brandName;
    private Long seriesId;
    private String seriesName;
    private Long typeId;
    private String typeName;
    private Long makerId;
    private String makerName;
    private Integer beforeQty;
    private Integer changeQty;
    private Integer afterQty;
    private BigDecimal price;
    private String currency;
    private String remark;
}

