package co.handk.backend.entity;

import co.handk.backend.annotation.UpdateIgnore;
import lombok.Data;

@Data
public class User extends BaseEntity {

    private String username;

    private Long deptId;
    @UpdateIgnore
    private String password;

    private String salt;

    private String email;

    private String phone;

    private String avatar;

    private Integer status;

}
