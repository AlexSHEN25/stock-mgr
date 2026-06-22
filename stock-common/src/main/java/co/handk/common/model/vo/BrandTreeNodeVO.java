package co.handk.common.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BrandTreeNodeVO {
    private Long id;
    private String name;
    private String englishName;
    private String image;
    private String content;
    private String nodeType;
    private Integer status;
    private Long brandId;
    private Long seriesId;
    private List<BrandTreeNodeVO> children = new ArrayList<>();
}
