package co.handk.common.model.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserRoleVO {

    private Long id;

    private Long userId;
    private Long roleId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
