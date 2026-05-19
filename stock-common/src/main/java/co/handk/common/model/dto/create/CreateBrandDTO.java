package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBrandDTO {

    @NotBlank(message = "名称は必須項目です")
    private String name;
    private String englishName;
    private String image;
    private String content;
}
