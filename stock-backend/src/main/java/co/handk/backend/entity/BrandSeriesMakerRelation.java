package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BrandSeriesMakerRelation extends BaseEntity {

    private Long brandId;

    private Long seriesId;

    private Long makerId;
}
