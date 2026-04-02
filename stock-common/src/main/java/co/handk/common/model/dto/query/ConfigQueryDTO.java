package co.handk.common.model.dto.query;

import lombok.Data;
import co.handk.common.model.PageQuery;

@Data
public class ConfigQueryDTO extends PageQuery {

    private Long id;

    private String name;
    private String group;
    private String title;
    private String tip;
    private String type;
    private String value;
    private String content;
}
