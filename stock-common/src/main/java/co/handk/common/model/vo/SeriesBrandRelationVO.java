package co.handk.common.model.vo;

import lombok.Data;

@Data
public class SeriesBrandRelationVO extends BaseVO {
    private Long seriesId;
    private String seriesName;
    private Long brandId;
    private String brandName;
}
