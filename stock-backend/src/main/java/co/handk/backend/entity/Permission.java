package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Permission extends BaseEntity {

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
