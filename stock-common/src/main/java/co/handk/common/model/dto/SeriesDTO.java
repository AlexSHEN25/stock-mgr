package co.handk.common.model.dto;

import lombok.Data;

@Data
public class SeriesDTO {

    private Long id;

    private String name;
    private String englishName;
    private String content;
    private Integer status;
}
