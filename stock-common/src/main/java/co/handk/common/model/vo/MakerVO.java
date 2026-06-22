package co.handk.common.model.vo;

import lombok.Data;

@Data
public class MakerVO extends BaseVO {
    private String name;
    private String englishName;
    private Long seriesId;
    private String seriesName;
    private Long brandId;
    private String brandName;
    private Integer status;
    private String statusDesc;
}
