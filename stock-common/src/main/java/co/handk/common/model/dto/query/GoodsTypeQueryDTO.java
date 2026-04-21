package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class GoodsTypeQueryDTO extends PageQuery {

    private Long id;

    private String name;
    private StatusEnum status;
}
