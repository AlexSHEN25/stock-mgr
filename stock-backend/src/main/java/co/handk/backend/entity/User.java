package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    private String username;

    private Integer groupId;

    private String password;

    private String salt;

    private String email;

    private String phone;

    private String avatar;

    private Integer status;

}