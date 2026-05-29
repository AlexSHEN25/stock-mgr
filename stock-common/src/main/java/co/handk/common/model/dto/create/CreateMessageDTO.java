package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateMessageDTO {

    private Integer type;
    @NotNull(message = "ユーザーIDは必須項目です")
    private Long userId;
    @NotBlank(message = "メッセージは必須項目です")
    private String message;
    private Integer sourceId;
    private Integer isRead;
    private Integer state;
}
