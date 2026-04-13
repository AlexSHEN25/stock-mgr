package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsSkuSpec extends BaseEntity {

    private Long skuId;

    private String skuCode;

    private Long specId;

    private String specName;

    private String specValue;

    private Integer sort;
}
