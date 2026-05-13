package co.handk.common.model.vo;

import co.handk.common.annotation.JoinSelect;
import lombok.Data;

@Data
public class SeriesVO extends BaseVO {
    private String name;
    private String englishName;

    private Long brandId;
    @JoinSelect("b.name")
    private String brandName;
    private String content;
    private Integer status;
    private String statusDesc;
}
