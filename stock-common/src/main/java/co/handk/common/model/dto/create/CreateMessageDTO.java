package co.handk.common.model.dto.create;

import lombok.Data;

@Data
public class CreateMessageDTO {

    private Integer type;
    private Long userId;
    private String message;
    private Integer sourceId;
    private Integer isRead;
    private Integer state;
}
