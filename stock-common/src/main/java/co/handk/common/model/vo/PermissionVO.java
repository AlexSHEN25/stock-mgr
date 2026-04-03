package co.handk.common.model.vo;

import lombok.Data;

@Data
public class PermissionVO extends BaseVO {
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
}
