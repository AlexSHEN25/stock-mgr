package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class RoleQueryDTO extends PageQuery {

    private Long id;

    private String name;
    private String code;
    private String remark;
    private StatusEnum status;
}
