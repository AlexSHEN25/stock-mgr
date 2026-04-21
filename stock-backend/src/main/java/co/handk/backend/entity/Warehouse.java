package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

@Data
@Schema(resource = "warehouse", name = "倉庫", group = "システム管理/在庫管理")
public class Warehouse extends BaseEntity {

    @SchemaField(title = "仓库名称")
    private String name;

    @SchemaField(title = "仓库编码")
    private String code;

    @SchemaField(title = "address")
    private String address;

    @SchemaField(title = "managerId")
    private Long managerId;

    @SchemaField(title = "status")
    private Integer status;
}