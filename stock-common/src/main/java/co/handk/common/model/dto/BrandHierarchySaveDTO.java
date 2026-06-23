package co.handk.common.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrandHierarchySaveDTO {

    private String id;

    private String nodeType;

    private Long brandId;

    @NotBlank(message = "brand name is required")
    private String brandName;

    private String brandEnglishName;

    private Long seriesId;

    private String seriesName;

    private String seriesEnglishName;

    private Long makerId;

    private String makerName;

    private String makerEnglishName;

    private Integer status;
}
