package co.handk.common.model.dto.query;

import co.handk.common.model.PageQuery;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RequestItemCartQueryDTO extends PageQuery {
    private Long customerId;
    private String customerName;
    private String groupCode;
    private Long goodsId;
    private String goodsName;
    private Long skuId;
    private String skuCode;
    private Long stockTypeId;
    private LocalDate startDate;
    private LocalDate endDate;
}
