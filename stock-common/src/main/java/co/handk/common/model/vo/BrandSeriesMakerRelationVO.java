package co.handk.common.model.vo;

import lombok.Data;

@Data
public class BrandSeriesMakerRelationVO extends BaseVO {
    private Long brandId;
    private Long seriesId;
    private Long makerId;
}
