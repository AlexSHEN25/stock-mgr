package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsImage extends BaseEntity {

    private Long goodsId;

    private Long skuId;

    private String skuCode;

    private String imageUrl;

    private Integer sort;
}
