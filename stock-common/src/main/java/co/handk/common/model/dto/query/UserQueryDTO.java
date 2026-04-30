package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class UserQueryDTO extends PageQuery {

    private String username;
    private Long deptId;
    private String email;
    private String phone;
    private StatusEnum status;
}
