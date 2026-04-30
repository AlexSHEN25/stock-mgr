package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseVO implements Serializable {

    @SchemaField(label = "ID", order = 1, editable = false)
    protected Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Tokyo")
    @SchemaField(label = "\u4f5c\u6210\u65e5\u6642", order = 98, editable = false)
    protected LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Tokyo")
    @SchemaField(label = "\u66f4\u65b0\u65e5\u6642", order = 99, editable = false)
    protected LocalDateTime updateTime;
}
