package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;

import lombok.Data;

@Data
public class CreateSeriesDTO {

    private String name;
    private String englishName;
    private String content;
    private StatusEnum status;
}
