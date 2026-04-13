package co.handk.common.model.dto.query;

import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class BrandMakerRelationQueryDTO extends PageQuery {

    private Long id;

    private Long brandId;

    private Long makerId;
}
