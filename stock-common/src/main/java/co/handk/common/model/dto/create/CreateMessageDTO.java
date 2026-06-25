package co.handk.common.model.dto.create;

import co.handk.common.jackson.BooleanIntegerDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateMessageDTO {

    private Integer type;
    private Long userId;
    @NotBlank(message = "メッセージは必須です")
    private String message;
    private Integer sourceId;
    @JsonDeserialize(using = BooleanIntegerDeserializer.class)
    private Integer isRead;
    private Integer state;
}
