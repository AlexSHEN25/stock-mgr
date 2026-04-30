package co.handk.common.model.dto.query;

import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class RolePermissionQueryDTO extends PageQuery {

    private Long id;

    private Long roleId;
    private Long permissionId;
}
