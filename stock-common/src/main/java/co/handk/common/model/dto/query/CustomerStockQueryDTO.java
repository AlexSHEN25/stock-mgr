package co.handk.common.model.dto.query;

import co.handk.common.model.PageQuery;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerStockQueryDTO extends PageQuery {
    private String country;
    private String groupCode;
    private Long customerId;
    private String customerName;
    private Long categoryId;
    private String categoryName;
    private Long goodsId;
    private String goodsName;
    private Long skuId;
    private String skuCode;
    private Long stockTypeId;
    private LocalDate startDate;
    private LocalDate endDate;
}
