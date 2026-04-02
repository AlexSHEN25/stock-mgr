package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;

import lombok.Data;
import co.handk.common.model.PageQuery;

@Data
public class DeptQueryDTO extends PageQuery {

    private Long id;

    private Long parentId;
    private String name;
    private String code;
    private Long leaderId;
    private Integer sort;
    private StatusEnum status;
}
