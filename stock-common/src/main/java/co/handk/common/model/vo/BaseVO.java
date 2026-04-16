package co.handk.common.model.vo;

import co.handk.schema.annotation.SchemaField;
import co.handk.schema.enums.FieldType;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseVO implements Serializable {

    /**
     * ID（表格展示，不可编辑，不参与搜索）
     */
    @SchemaField(
            title = "ID",
            type = FieldType.INPUT,
            table = true,
            search = false,
            editable = false,
            detail = true
    )
    private Long id;

    /**
     * 创建时间（只展示）
     */
    @SchemaField(
            title = "创建时间",
            type = FieldType.DATETIME,
            table = true,
            search = false,
            editable = false,
            detail = true
    )
    private LocalDateTime createTime;

    /**
     * 更新时间（只展示）
     */
    @SchemaField(
            title = "更新时间",
            type = FieldType.DATETIME,
            table = true,
            search = false,
            editable = false,
            detail = true
    )
    private LocalDateTime updateTime;
}