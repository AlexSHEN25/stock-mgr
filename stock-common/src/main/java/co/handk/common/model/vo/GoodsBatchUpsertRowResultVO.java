package co.handk.common.model.vo;

import lombok.Data;

@Data
public class GoodsBatchUpsertRowResultVO {

    private Integer rowNo;
    private Boolean success;
    private String action;
    private Long goodsId;
    private Long skuId;
    private String skuCode;
    private String message;
}
