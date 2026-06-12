package co.handk.common.model.vo;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class CustomerGoodsMatrixRowVO {
    private Long goodsId;
    private String goodsName;
    private Long categoryId;
    private String categoryName;
    private Long totalQuantity;
    private Map<String, Long> quantities = new LinkedHashMap<>();
}
