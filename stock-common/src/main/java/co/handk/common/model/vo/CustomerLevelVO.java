package co.handk.common.model.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CustomerLevelVO {

    private Long id;

    private String name;
    private BigDecimal discount;
    private String remark;
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
