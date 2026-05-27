package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class PermissionQueryDTO extends PageQuery {
    private String module;
    private Integer type;
    private Long parentId;
    private StatusEnum status;
}
