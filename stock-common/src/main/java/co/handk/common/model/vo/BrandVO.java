package co.handk.common.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class BrandVO extends BaseVO {
    private String name;
    private String englishName;
    private String image;
    private String content;
    private Integer status;
    private String statusDesc;
    private List<Long> seriesIds;
    private List<Long> makerIds;
    private List<String> seriesNames;
    private List<String> makerNames;
}
