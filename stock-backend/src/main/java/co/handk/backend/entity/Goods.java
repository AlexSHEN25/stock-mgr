package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

@Data
@Schema(resource = "goods", name = "商品", group = "システム管理/商品管理")
public class Goods extends BaseEntity {

    @SchemaField(title = "商品名称")
    private String name;

    @SchemaField(title = "英文品名")
    private String englishName;

    @SchemaField(title = "系列ID")
    private Long seriesId;

    @SchemaField(title = "品牌ID")
    private Long brandId;

    @SchemaField(title = "商品类型ID")
    private Long categoryId;

    @SchemaField(title = "厂家ID")
    private Long makerId;

    @SchemaField(title = "状态:1上架2下架")
    private Integer status;

    @SchemaField(title = "商品描述")
    private String description;

    @SchemaField(title = "是否热门")
    private Integer isHot;

    @SchemaField(title = "排序")
    private Integer sort;
}