package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BrandMakerRelation extends BaseEntity {

    private Long brandId;

    private Long makerId;
}
