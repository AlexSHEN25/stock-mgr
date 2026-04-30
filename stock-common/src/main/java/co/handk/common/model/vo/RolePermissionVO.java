package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

@Data
public class RolePermissionVO extends BaseVO {
    @SchemaField(label = "\u30ed\u30fc\u30ebID", order = 50)
    private Long roleId;
    @SchemaField(label = "\u6a29\u9650ID", order = 50)
    private Long permissionId;
}
