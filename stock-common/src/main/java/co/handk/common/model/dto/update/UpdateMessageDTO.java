package co.handk.common.model.dto.update;

import co.handk.common.jackson.BooleanIntegerDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
    @JsonDeserialize(using = BooleanIntegerDeserializer.class)
    private Integer isRead;
    private Integer state;
}
