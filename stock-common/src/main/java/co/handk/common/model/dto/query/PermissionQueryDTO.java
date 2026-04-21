package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class PermissionQueryDTO extends PageQuery {

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
    private StatusEnum status;
}
