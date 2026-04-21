package co.handk.common.model.vo;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaDict;
import co.handk.schema.annotation.SchemaField;
import co.handk.schema.enums.FieldType;
import lombok.Data;

@Data
@Schema(
        resource = "dept",
        name = "部门管理",
        group = "系统管理"
)
public class DeptVO extends BaseVO {

    @SchemaField(
            title = "ID",
            table = false,
            editable = false
    )
    private Long id;

    @SchemaField(
            title = "部门名称",
            search = true
    )
    private String name;

    @SchemaField(title = "编码")
    private String code;

    @SchemaField(title = "排序")
    private Integer sort;

    @SchemaField(
            title = "状态",
            type = FieldType.SELECT,
            dict = @SchemaDict(code = "common_status")
    )
    private Integer status;
}