package co.handk.common.model.dto;

import lombok.Data;

@Data
public class ConfigDTO {

    private Long id;

    private String name;
    private String group;
    private String title;
    private String tip;
    private String type;
    private String value;
    private String content;
}
