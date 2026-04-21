package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import co.handk.schema.annotation.SchemaRef;
import co.handk.schema.enums.FieldType;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(resource = "user", name = "ユーザー", group = "システム管理/ユーザー管理")
public class User extends BaseEntity {

    @SchemaField(
            title = "用户名",
            search = true
    )
    private String username;

    /**
     * deptId：用于提交 & 查询（隐藏展示）
     */
    @SchemaField(
            title = "部门ID",
            type = FieldType.SELECT,
            table = false,
            search = true, // 查询用ID
            ref = @SchemaRef(
                    resource = "dept",
                    labelField = "name",
                    valueField = "id",
                    displayField = "deptName"
            )
    )
    private Long deptId;

    /**
     * deptName：只用于展示（虚拟字段）
     */
    @SchemaField(
            title = "部门名称",
            editable = false
    )
    @TableField(exist = false)
    private String deptName;

    /**
     * 密码（完全隐藏）
     */
    @SchemaField(
            title = "密码",
            table = false,
            search = false,
            detail = false,
            editable = false
    )
    private String password;

    /**
     * 盐（完全隐藏）
     */
    @SchemaField(
            title = "密码盐",
            table = false,
            search = false,
            detail = false,
            editable = false
    )
    private String salt;

    @SchemaField(title = "电子邮箱")
    private String email;

    @SchemaField(title = "联系方式")
    private String phone;

    @SchemaField(title = "头像", type = FieldType.UPLOAD)
    private String avatar;

    @SchemaField(title = "状态", type = FieldType.SWITCH)
    private Integer status;
}
