package co.handk.common.model.vo;

import lombok.Data;

@Data
public class CustomerGoodsMatrixCellVO {
    private Long goodsId;
    private Long customerId;
    private Long quantity;
}
