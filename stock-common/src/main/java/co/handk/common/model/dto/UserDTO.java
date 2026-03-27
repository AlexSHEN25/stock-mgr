package co.handk.common.model.dto;

import lombok.Data;

@Data
public class UserDTO {

    private Long id;

    private String username;
    private Long deptId;
    private String password;
    private String salt;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
}
