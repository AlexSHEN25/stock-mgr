package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerLevelVO extends BaseVO {
    private String name;
    private BigDecimal discount;
    private String remark;
    private Integer status;
    private String statusDesc;
}
