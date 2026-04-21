package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(resource = "customerLevel", name = "顧客ランク", group = "システム管理/顧客管理")
public class CustomerLevel extends BaseEntity {

    @SchemaField(title = "等级名称")
    private String name;

    @SchemaField(title = "默认折扣")
    private BigDecimal discount;

    @SchemaField(title = "备注")
    private String remark;

    @SchemaField(title = "状态")
    private Integer status;
}