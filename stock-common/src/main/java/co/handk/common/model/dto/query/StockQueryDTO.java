package co.handk.common.model.dto.query;
import co.handk.common.enums.StatusEnum;
import lombok.Data;
import co.handk.common.model.PageQuery;
@Data
public class StockQueryDTO extends PageQuery {
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
    private StatusEnum status;
}
