package co.handk.common.model.vo;

import lombok.Data;

@Data
public class UserRoleVO extends BaseVO {
    private Long userId;
    private Long roleId;
}
