package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

@Data
@Schema(resource = "stockType", name = "在庫区分", group = "システム管理/在庫管理")
public class StockType extends BaseEntity {

    @SchemaField(title = "库存分类名称(常规品，不良品)")
    private String name;

    @SchemaField(title = "状态:1启用0停用")
    private Integer status;
}