package co.handk.common.model.dto;

import lombok.Data;

@Data
public class BrandDTO {

    private Long id;

    private String name;
    private String englishName;
    private String image;
    private String content;
    private Integer status;
}
