package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateConfigDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private String name;
    private String group;
    private String title;
    private String tip;
    private String type;
    private String value;
    private String content;
}
