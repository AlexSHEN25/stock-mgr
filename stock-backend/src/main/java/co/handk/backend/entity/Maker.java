package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

@Data
@Schema(resource = "maker", name = "メーカー", group = "システム管理/商品管理")
public class Maker extends BaseEntity {

    @SchemaField(title = "厂家名称")
    private String name;

    @SchemaField(title = "状态:1启用0停用")
    private Integer status;
}