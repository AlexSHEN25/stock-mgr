package co.handk.common.model.dto.query;

import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class MessageQueryDTO extends PageQuery {

    private Long id;

    private Integer type;
    private Long userId;
    private String message;
    private Integer sourceId;
    private Integer isRead;
    private Integer state;
}
