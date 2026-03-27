package co.handk.common.model.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CustomerLevelDTO {

    private Long id;

    private String name;
    private BigDecimal discount;
    private String remark;
    private Integer status;
}
