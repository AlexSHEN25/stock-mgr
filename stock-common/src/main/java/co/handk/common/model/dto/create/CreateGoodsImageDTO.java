package co.handk.common.model.dto.create;

import lombok.Data;

@Data
public class CreateGoodsImageDTO {

    private Long goodsId;

    private Long skuId;

    private String skuCode;

    private String imageUrl;

    private Integer sort;
}
