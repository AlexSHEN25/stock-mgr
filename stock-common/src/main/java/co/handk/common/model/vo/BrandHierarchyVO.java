package co.handk.common.model.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BrandHierarchyVO {
    private String id;
    private String nodeType;
    private Long brandId;
    private String brandName;
    private String brandEnglishName;
    private Long seriesId;
    private String seriesName;
    private String seriesEnglishName;
    private Long makerId;
    private String makerName;
    private String makerEnglishName;
    private Integer status;
    private LocalDateTime updateTime;
}
