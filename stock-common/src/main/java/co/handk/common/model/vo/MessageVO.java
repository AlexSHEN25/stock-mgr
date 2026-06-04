package co.handk.common.model.vo;

import co.handk.common.annotation.JoinValue;
import lombok.Data;

@Data
public class MessageVO extends BaseVO {
    private Integer type;
    private Long userId;
    @JoinValue(sourceField = "userId", serviceBean = "userServiceImpl", targetField = "username")
    private String username;
    private String message;
    private Integer sourceId;
    private Integer isRead;
    private Integer state;
}
