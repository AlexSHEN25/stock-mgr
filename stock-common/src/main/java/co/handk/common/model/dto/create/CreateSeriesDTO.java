package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import co.handk.common.enums.StatusEnum;
import lombok.Data;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateSeriesDTO {

    @NotBlank(message = "必須項目です")
    private String name;
    private String englishName;
    @NotNull(message = "必須項目です")
    private Long brandId;
    private String content;
    private StatusEnum status;

}
