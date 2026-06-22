package co.handk.common.model.dto;

import lombok.Data;

@Data
public class BrandSeriesMakerRelationItemDTO {
    private Long brandId;
    private Long seriesId;
    private Long makerId;
}
