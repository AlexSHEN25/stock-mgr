package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

@Data
public class PermissionVO extends BaseVO {
    @SchemaField(label = "\u540d\u79f0", order = 50)
    private String name;
    @SchemaField(label = "\u30b3\u30fc\u30c9", order = 50)
    private String code;
    @SchemaField(label = "\u30e2\u30b8\u30e5\u30fc\u30eb", order = 50)
    private String module;
    @SchemaField(label = "\u7a2e\u5225", order = 50)
    private Integer type;
    @SchemaField(label = "\u89aaID", order = 50)
    private Long parentId;
    @SchemaField(label = "\u30d1\u30b9", order = 50)
    private String path;
    @SchemaField(label = "\u4e26\u3073\u9806", order = 50)
    private Integer sort;
    @SchemaField(label = "\u30a2\u30a4\u30b3\u30f3", order = 50)
    private String icon;
    @SchemaField(label = "\u30b3\u30f3\u30dd\u30fc\u30cd\u30f3\u30c8", order = 50)
    private String component;
    @SchemaField(label = "\u72b6\u614b\u30b3\u30fc\u30c9", order = 40)
    private Integer status;
    @SchemaField(label = "\u72b6\u614b", order = 41)
    private String statusDesc;
}
