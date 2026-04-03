package co.handk.common.model.vo;

import lombok.Data;

@Data
public class ConfigVO extends BaseVO {
    private String name;
    private String group;
    private String title;
    private String tip;
    private String type;
    private String value;
    private String content;
}
