package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotBlank;
import co.handk.common.enums.StatusEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class UpdateBrandDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

    @NotBlank(message = "名称は必須項目です")
    private String name;
    private String englishName;
    private String image;
    private String content;
    private StatusEnum status;
}
