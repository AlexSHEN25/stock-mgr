package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateConfigDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

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
