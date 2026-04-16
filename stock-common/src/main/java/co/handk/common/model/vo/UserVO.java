package co.handk.common.model.vo;

import co.handk.schema.annotation.*;
import co.handk.schema.enums.FieldType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(
        resource = "user",
        name = "用户管理",
        group = "系统管理"
)
public class UserVO extends BaseVO {

    @SchemaField(
            title = "ID",
            table = true,
            editable = false
    )
    private Long id;

    @SchemaField(
            title = "用户名",
            search = true,
            required = true
    )
    private String username;

    @SchemaField(title = "邮箱")
    private String email;

    @SchemaField(title = "手机号")
    private String phone;

    /**
     * deptId：查询 + 提交
     */
    @SchemaField(
            title = "部门",
            type = FieldType.SELECT,
            search = true,
            ref = @SchemaRef(
                    resource = "dept",
                    labelField = "name",
                    valueField = "id"
            )
    )
    private Long deptId;

    /**
     * deptName：只展示
     */
    @SchemaField(
            title = "部门名称",
            editable = false
    )
    private String deptName;

    @SchemaField(
            title = "状态",
            type = FieldType.SELECT,
            dict = @SchemaDict(code = "user_status")
    )
    private Integer status;

    @SchemaField(
            title = "创建时间",
            type = FieldType.DATETIME,
            editable = false
    )
    private LocalDateTime createTime;
}