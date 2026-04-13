package co.handk.common.model.vo;

import lombok.Data;

@Data
public class GoodsSkuSpecVO extends BaseVO {
    private Long skuId;
    private String skuCode;
    private Long specId;
    private String specName;
    private String specValue;
    private Integer sort;
}
