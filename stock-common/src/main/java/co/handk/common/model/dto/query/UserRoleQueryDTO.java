package co.handk.common.model.dto.query;

import lombok.Data;
import co.handk.common.model.PageQuery;

@Data
public class UserRoleQueryDTO extends PageQuery {

    private Long id;

    private Long userId;
    private Long roleId;
}
