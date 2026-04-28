package co.handk.common.model.vo;

import lombok.Data;

@Data
public class BrandVO extends BaseVO {
    private String name;
    private String englishName;
    private String image;
    private String content;
    private Integer status;
    private String statusDesc;
}
