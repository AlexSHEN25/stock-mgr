package co.handk.common.model.dto.create;

import lombok.Data;

@Data
public class CreateConfigDTO {

    private String name;
    private String group;
    private String title;
    private String tip;
    private String type;
    private String value;
    private String content;
}
