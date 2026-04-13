package co.handk.common.model.dto.query;

import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class GoodsSkuSpecQueryDTO extends PageQuery {

    private Long id;

    private Long skuId;

    private String skuCode;

    private Long specId;

    private String specName;

    private String specValue;

    private Integer sort;
}
