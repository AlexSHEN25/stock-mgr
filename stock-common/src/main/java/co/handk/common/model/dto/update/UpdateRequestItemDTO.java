package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class UpdateRequestItemDTO {
    @NotNull(message = "ID荳崎・荳ｺ遨ｺ")
    private Long id;

    private Long requestId;
    private Long goodsId;
    private Long skuId;
    private String skuCode;
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
    private Long warehouseId;
    private BigDecimal price;
    private String currency;
    private BigDecimal discount;
    private Integer requestQty;
    private Integer approveQty;
    private Integer outQty;
    private Long stockRecordId;
    private String remark;
}

