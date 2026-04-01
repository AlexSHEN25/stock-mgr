package co.handk.common.model.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ConfigVO {

    private Long id;

    private String name;
    private String group;
    private String title;
    private String tip;
    private String type;
    private String value;
    private String content;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
