package co.handk.common.model.vo;

import lombok.Data;

@Data
public class BrandSeriesMakerRelationItemVO {
    private Long brandId;
    private String brandName;
    private Long seriesId;
    private String seriesName;
    private Long makerId;
    private String makerName;
}
