package co.handk.common.model.vo;

import lombok.Data;

@Data
public class MessageVO extends BaseVO {
    private Integer type;
    private Long userId;
    private String message;
    private Integer sourceId;
    private Integer isRead;
    private Integer state;
}
