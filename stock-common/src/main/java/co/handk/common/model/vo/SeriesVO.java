package co.handk.common.model.vo;

import lombok.Data;

@Data
public class SeriesVO extends BaseVO {
    private String name;
    private String englishName;
    private String content;
    private Integer status;
}
