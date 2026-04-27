package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSeriesDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private String name;
    private String englishName;
    private Long brandId;
    private String content;
    private StatusEnum status;
}
