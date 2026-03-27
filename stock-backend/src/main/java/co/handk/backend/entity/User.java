package co.handk.backend.entity;

import lombok.Data;

@Data
public class User extends BaseEntity {

    private String username;

    private Long deptId;

    private String password;

    private String salt;

    private String email;

    private String phone;

    private String avatar;

    private Integer status;

}
