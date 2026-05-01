package co.handk.common.model.vo;

import lombok.Data;

@Data
public class MakerVO extends BaseVO {
    private String name;
    private Integer status;
    private String statusDesc;
}
