package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;

import lombok.Data;
import co.handk.common.model.PageQuery;

@Data
public class WarehouseQueryDTO extends PageQuery {

    private Long id;

    private String name;
    private String code;
    private String address;
    private Long managerId;
    private StatusEnum status;
}
