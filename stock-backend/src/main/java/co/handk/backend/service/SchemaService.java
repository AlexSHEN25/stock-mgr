package co.handk.backend.service;

import co.handk.backend.model.schema.MenuItemVO;
import co.handk.backend.model.schema.SchemaVO;

import java.util.List;

public interface SchemaService {
    SchemaVO getSchema(String name);

    List<MenuItemVO> getMenuSchema();
}
