package co.handk.common.model.dto.query;

import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class GoodsImageQueryDTO extends PageQuery {

    private Long id;

    private Long goodsId;

    private Long skuId;

    private String skuCode;

    private String imageUrl;

    private Integer sort;
}
