package co.handk.common.model.vo;

import lombok.Data;

@Data
public class UserVO extends BaseVO {

    private String username;
    private Long deptId;

    private String deptName;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;

}
