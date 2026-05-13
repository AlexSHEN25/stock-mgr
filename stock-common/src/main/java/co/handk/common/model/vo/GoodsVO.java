package co.handk.common.model.vo;

import co.handk.common.annotation.JoinSelect;
import lombok.Data;

@Data
public class GoodsVO extends BaseVO {
    private String name;
    private String englishName;
    private Long brandId;
    @JoinSelect("b.name")
    private String brandName;
    private Long seriesId;
    @JoinSelect("s.name")
    private String seriesName;
    private Long categoryId;
    @JoinSelect("c.name")
    private String categoryName;
    private Long makerId;
    @JoinSelect("m.name")
    private String makerName;
    private String description;
    private Integer isHot;
    private Integer sort;
    private Integer status;
    private String statusDesc;
}
