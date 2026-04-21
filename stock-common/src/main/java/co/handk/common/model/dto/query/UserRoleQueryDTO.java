package co.handk.common.model.dto.query;

import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class UserRoleQueryDTO extends PageQuery {

    private Long id;

    private Long userId;
    private Long roleId;
}
