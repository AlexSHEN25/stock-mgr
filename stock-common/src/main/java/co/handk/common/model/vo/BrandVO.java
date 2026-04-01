package co.handk.common.model.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BrandVO {

    private Long id;

    private String name;
    private String englishName;
    private String image;
    private String content;
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
