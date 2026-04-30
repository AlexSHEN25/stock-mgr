package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class WarehouseQueryDTO extends PageQuery {

    private Long id;

    private String name;
    private String code;
    private String address;
    private Long managerId;
    private StatusEnum status;
}
