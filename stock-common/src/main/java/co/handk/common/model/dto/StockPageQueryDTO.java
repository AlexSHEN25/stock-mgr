package co.handk.common.model.dto;

import co.handk.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StockPageQueryDTO extends PageQuery {

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * SKU / 品番
     */
    private String sku;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 状态
     */
    private Integer status;
}