package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;

import lombok.Data;
import co.handk.common.model.PageQuery;

@Data
public class RoleQueryDTO extends PageQuery {

    private Long id;

    private String name;
    private String code;
    private String remark;
    private StatusEnum status;
}
