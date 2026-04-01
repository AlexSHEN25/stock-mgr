package co.handk.common.model.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PermissionVO {

    private Long id;

    private String name;
    private String code;
    private String module;
    private Integer type;
    private Long parentId;
    private String path;
    private Integer sort;
    private String icon;
    private String component;
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
