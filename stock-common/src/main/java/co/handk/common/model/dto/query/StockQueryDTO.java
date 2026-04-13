package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class StockQueryDTO extends PageQuery {
    private String goodsName;
    private String skuCode;
    private Long skuId;
    private Integer typeId;
    private String currency;
    private Long warehouseId;
    private StatusEnum status;
}

