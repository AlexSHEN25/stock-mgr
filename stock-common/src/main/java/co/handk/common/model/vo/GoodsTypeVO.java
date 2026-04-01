package co.handk.common.model.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class GoodsTypeVO {

    private Long id;

    private String name;
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
