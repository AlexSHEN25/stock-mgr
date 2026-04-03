package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;

import lombok.Data;
import co.handk.common.model.PageQuery;

@Data
public class UserQueryDTO extends PageQuery {

    private String username;
    private Long deptId;
    private String email;
    private String phone;
    private StatusEnum status;
}
