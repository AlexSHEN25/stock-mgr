package co.handk.common.model.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserVO {

    private Long id;

    private String username;
    private Long deptId;

    private String deptName;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
