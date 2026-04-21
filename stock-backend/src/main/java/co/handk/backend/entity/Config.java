package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
@Schema(resource = "config", name = "システム設定", group = "システム管理/システム設定管理")
public class Config extends BaseEntity {

    @SchemaField(title = "变量名")
    private String name;

    @TableField("`group`")
    @SchemaField(title = "分组")
    private String group;

    @SchemaField(title = "变量标题")
    private String title;

    @SchemaField(title = "变量描述")
    private String tip;

    @SchemaField(title = "类型:string,text,int,bool,array,datetime,date,file")
    private String type;

    @SchemaField(title = "变量值")
    private String value;

    @SchemaField(title = "变量字典数据")
    private String content;
}