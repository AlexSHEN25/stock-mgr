package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateMessageDTO {

    private Integer type;
    private Long userId;
    @NotBlank(message = "message is required")
    private String message;
    private Integer sourceId;
    private Integer isRead;
    private Integer state;
}
