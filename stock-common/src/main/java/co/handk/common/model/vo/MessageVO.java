package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

@Data
public class MessageVO extends BaseVO {
    @SchemaField(label = "\u7a2e\u5225", order = 50)
    private Integer type;
    @SchemaField(label = "\u30e6\u30fc\u30b6\u30fcID", order = 50)
    private Long userId;
    @SchemaField(label = "\u30e6\u30fc\u30b6\u30fc\u540d", order = 50)
    private String username;
    @SchemaField(label = "\u30e1\u30c3\u30bb\u30fc\u30b8", order = 50)
    private String message;
    @SchemaField(label = "\u5143\u30c7\u30fc\u30bfID", order = 50)
    private Integer sourceId;
    @SchemaField(label = "\u65e2\u8aad", order = 50)
    private Integer isRead;
    @SchemaField(label = "\u72b6\u614b", order = 50)
    private Integer state;
}
