package co.handk.common.model.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RoleVO {

    private Long id;

    private String name;
    private String code;
    private String remark;
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
