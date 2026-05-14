package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class GoodsQueryDTO extends PageQuery {

    private Long id;

    private String name;
    private String englishName;
    private Long seriesId;
    private Long brandId;
    private Long categoryId;
    private Long makerId;
    private Integer sort;
    private StatusEnum status;
    private Integer isHot;
}
