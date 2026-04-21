package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

@Data
@Schema(resource = "brandMakerRelation", name = "メーカー関連", group = "システム管理/商品管理")
public class BrandMakerRelation extends BaseEntity {

    @SchemaField(title = "品牌ID")
    private Long brandId;

    @SchemaField(title = "厂家ID")
    private Long makerId;
}