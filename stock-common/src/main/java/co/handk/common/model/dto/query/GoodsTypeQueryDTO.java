package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;

import lombok.Data;
import co.handk.common.model.PageQuery;

@Data
public class GoodsTypeQueryDTO extends PageQuery {

    private Long id;

    private String name;
    private StatusEnum status;
}
