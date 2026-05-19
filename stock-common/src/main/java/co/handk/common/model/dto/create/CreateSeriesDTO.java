package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSeriesDTO {

    @NotBlank(message = "名称は必須項目です")
    private String name;
    private String englishName;
    @NotNull(message = "ブランドは必須項目です")
    private Long brandId;
    private String content;
}
