package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMessageDTO {
    @NotNull(message = "IDは必須です")
    private Long id;

    private Integer type;
    private Long userId;
    @NotBlank(message = "メッセージは必須です")
    private String message;
    private Integer sourceId;
    private Integer isRead;
    private Integer state;
}
