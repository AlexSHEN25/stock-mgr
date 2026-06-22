package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class MakerQueryDTO extends PageQuery {

    private String name;
    private String englishName;
    private Long brandId;
    private Long seriesId;
    private StatusEnum status;
}
