package co.handk.common.model.dto.query;

import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class BrandHierarchyQueryDTO extends PageQuery {
    private String brandName;
    private String brandEnglishName;
    private String seriesName;
    private String seriesEnglishName;
    private String makerName;
    private String makerEnglishName;
    private Integer status;
}
