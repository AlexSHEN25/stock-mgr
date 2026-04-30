package co.handk.backend.model.schema;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SchemaVO {
    private String name;
    private List<SchemaColumnVO> columns;
}
