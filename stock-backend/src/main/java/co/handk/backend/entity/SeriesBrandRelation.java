package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SeriesBrandRelation extends BaseEntity {

    private Long seriesId;

    private Long brandId;
}
