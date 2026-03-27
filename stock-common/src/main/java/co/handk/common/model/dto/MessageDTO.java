package co.handk.common.model.dto;

import lombok.Data;

@Data
public class MessageDTO {

    private Long id;

    private Integer type;
    private Long userId;
    private String message;
    private Long sourceId;
    private Integer isRead;
    private Integer state;
}
