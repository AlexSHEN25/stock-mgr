package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

@Data
public class LoginVO extends BaseVO {

    @SchemaField(label = "\u30c8\u30fc\u30af\u30f3", order = 50)
    private String token;

    @SchemaField(label = "\u30e6\u30fc\u30b6\u30fcID", order = 50)
    private Long userId;

    @SchemaField(label = "\u30e6\u30fc\u30b6\u30fc\u540d", order = 50)
    private String username;

}
