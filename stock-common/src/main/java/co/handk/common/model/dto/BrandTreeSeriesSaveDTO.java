package co.handk.common.model.dto;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class BrandTreeSeriesSaveDTO {

    private Long id;

    @NotBlank(message = "series name is required")
    private String name;

    private String englishName;

    private String content;

    private StatusEnum status;

    @Valid
    private List<BrandTreeMakerSaveDTO> makers = new ArrayList<>();
}
