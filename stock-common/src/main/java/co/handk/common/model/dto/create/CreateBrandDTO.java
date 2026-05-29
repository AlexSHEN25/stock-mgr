package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import co.handk.common.enums.StatusEnum;
import lombok.Data;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateBrandDTO {

    @NotBlank(message = "必須項目です")
    private String name;
    private String englishName;
    private String image;
    private String content;
    private StatusEnum status;

}
