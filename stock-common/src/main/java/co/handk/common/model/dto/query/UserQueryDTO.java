package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;

import lombok.Data;
import co.handk.common.model.PageQuery;

@Data
public class UserQueryDTO extends PageQuery {

    private Long id;
    private String username;

    private Long deptId;
    private String password;
    private String email;
    private String phone;
    private String avatar;
    private StatusEnum status;
}
