package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateConfigDTO {

    @NotBlank(message = "名称は必須項目です")
    private String name;
    @NotBlank(message = "グループは必須項目です")
    private String group;
    @NotBlank(message = "タイトルは必須項目です")
    private String title;
    @NotBlank(message = "ヒントは必須項目です")
    private String tip;
    @NotBlank(message = "タイプは必須項目です")
    private String type;
    @NotBlank(message = "値は必須項目です")
    private String value;
    private String content;
}
