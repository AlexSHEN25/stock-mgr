package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageQuery;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerLevelQueryDTO extends PageQuery {

    private Long id;

    private String name;
    private BigDecimal discount;
    private String remark;
    private StatusEnum status;
}
