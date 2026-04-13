package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Message extends BaseEntity {

    private Integer type;

    private Long userId;

    private String message;

    private Integer sourceId;

    private Integer isRead;

    private Integer state;
}
