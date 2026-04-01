package co.handk.common.model.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SeriesVO {

    private Long id;

    private String name;
    private String englishName;
    private String content;
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
