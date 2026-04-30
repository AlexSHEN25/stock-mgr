package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

@Data
public class ConfigVO extends BaseVO {
    @SchemaField(label = "\u540d\u79f0", order = 50)
    private String name;
    @SchemaField(label = "\u30b0\u30eb\u30fc\u30d7", order = 50)
    private String group;
    @SchemaField(label = "\u30bf\u30a4\u30c8\u30eb", order = 50)
    private String title;
    @SchemaField(label = "\u30d2\u30f3\u30c8", order = 50)
    private String tip;
    @SchemaField(label = "\u7a2e\u5225", order = 50)
    private String type;
    @SchemaField(label = "\u5024", order = 50)
    private String value;
    @SchemaField(label = "\u5185\u5bb9", order = 50)
    private String content;
}
