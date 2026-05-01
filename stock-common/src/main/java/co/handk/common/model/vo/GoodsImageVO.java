package co.handk.common.model.vo;

import lombok.Data;

@Data
public class GoodsImageVO extends BaseVO {
    private Long goodsId;
    private Long skuId;
    private String skuCode;
    private String imageUrl;
    private Integer sort;
}
