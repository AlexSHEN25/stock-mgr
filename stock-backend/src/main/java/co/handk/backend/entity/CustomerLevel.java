package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerLevel extends BaseEntity {

    private String name;

    private BigDecimal discount;

    private String remark;

    private Integer status;
}
