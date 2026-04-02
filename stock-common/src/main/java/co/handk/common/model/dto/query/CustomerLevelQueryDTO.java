package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;

import java.math.BigDecimal;
import lombok.Data;
import co.handk.common.model.PageQuery;

@Data
public class CustomerLevelQueryDTO extends PageQuery {

    private Long id;

    private String name;
    private BigDecimal discount;
    private String remark;
    private StatusEnum status;
}
