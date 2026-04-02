package co.handk.common.model.dto.query;

import lombok.Data;
import co.handk.common.model.PageQuery;

@Data
public class MessageQueryDTO extends PageQuery {

    private Long id;

    private Integer type;
    private Long userId;
    private String message;
    private Long sourceId;
    private Integer isRead;
    private Integer state;
}
