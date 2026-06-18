package co.handk.common.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class MakerVO extends BaseVO {
    private String name;
    private Integer status;
    private String statusDesc;
    private List<Long> brandIds;
    private List<String> brandNames;
}
