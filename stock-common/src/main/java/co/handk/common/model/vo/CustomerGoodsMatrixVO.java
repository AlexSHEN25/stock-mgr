package co.handk.common.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class CustomerGoodsMatrixVO {
    private Long total;
    private Long pageNum;
    private Long pageSize;
    private Long totalPages;
    private List<CustomerGoodsMatrixColumnVO> columns;
    private List<CustomerGoodsMatrixRowVO> rows;
}
