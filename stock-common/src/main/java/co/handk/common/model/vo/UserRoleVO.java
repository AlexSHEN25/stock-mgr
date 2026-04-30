package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

@Data
public class UserRoleVO extends BaseVO {
    @SchemaField(label = "\u30e6\u30fc\u30b6\u30fcID", order = 50)
    private Long userId;
    @SchemaField(label = "\u30ed\u30fc\u30ebID", order = 50)
    private Long roleId;
}
