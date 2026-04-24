package co.handk.common.model.dto.create;

import lombok.Data;

@Data
public class CreateGoodsDTO {

    private String name;
    private String englishName;
    private Long brandId;
    private Long seriesId;
    private Long categoryId;
    private Long makerId;
    private String description;
    private Integer isHot;
    private Integer sort;
}
