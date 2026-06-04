package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMessageDTO {
    @NotNull(message = "id is required")
    private Long id;

    private Integer type;
    private Long userId;
    @NotBlank(message = "message is required")
    private String message;
    private Integer sourceId;
    private Integer isRead;
    private Integer state;
}
