package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageQuery;
import lombok.Data;

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
