package co.handk.common.model.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MessageVO {

    private Long id;

    private Integer type;
    private Long userId;
    private String message;
    private Long sourceId;
    private Integer isRead;
    private Integer state;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
