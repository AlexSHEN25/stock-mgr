package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMessageDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private Integer type;
    private Long userId;
    private String message;
    private Integer sourceId;
    private Integer isRead;
    private Integer state;
}
