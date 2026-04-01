package co.handk.common.model.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RolePermissionVO {

    private Long id;

    private Long roleId;
    private Long permissionId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
