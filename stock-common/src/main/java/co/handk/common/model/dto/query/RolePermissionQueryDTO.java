package co.handk.common.model.dto.query;

import lombok.Data;
import co.handk.common.model.PageQuery;

@Data
public class RolePermissionQueryDTO extends PageQuery {

    private Long id;

    private Long roleId;
    private Long permissionId;
}
