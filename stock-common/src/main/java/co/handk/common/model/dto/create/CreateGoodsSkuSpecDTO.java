package co.handk.common.model.dto.create;

import lombok.Data;

@Data
public class CreateGoodsSkuSpecDTO {

    private Long skuId;

    private String skuCode;

    private Long specId;

    private String specName;

    private String specValue;

    private Integer sort;
}
