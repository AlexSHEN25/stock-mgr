package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class StockQueryDTO extends PageQuery {
    /**
     * 蝠・刀蜷咲ｧｰ
     */
    private String goodsName;
    /**
     * SKU / 蜩∫分
     */
    private String skuCode;
    private Long skuId;
    private Integer typeId;
    private String currency;
    /**
     * 莉灘ｺ的D
     */
    private Long warehouseId;
    /**
     * 迥ｶ諤・
     */
    private StatusEnum status;
}

