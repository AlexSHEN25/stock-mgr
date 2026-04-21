package co.handk.common.model.dto.query;

import co.handk.common.model.PageQuery;
import lombok.Data;

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
