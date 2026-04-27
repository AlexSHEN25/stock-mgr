package co.handk.common.model.dto.create;

import lombok.Data;

@Data
public class CreateSeriesDTO {

    private String name;
    private String englishName;
    private Long brandId;
    private String content;
}
