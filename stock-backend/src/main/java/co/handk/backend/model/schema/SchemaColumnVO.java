package co.handk.backend.model.schema;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SchemaColumnVO {
    private String field;
    private String label;
    private Integer order;
    private Boolean visible;
    private Boolean editable;
    private String controlType;
    private String dataType;
}
