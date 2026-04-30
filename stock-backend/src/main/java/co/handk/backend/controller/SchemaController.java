package co.handk.backend.controller;

import co.handk.backend.model.schema.MenuItemVO;
import co.handk.backend.service.SchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/schema")
@RequiredArgsConstructor
public class SchemaController {

    private final SchemaService schemaService;

//    @GetMapping("/{name}")
//    public SchemaVO getSchema(@PathVariable("name") String name) {
//        return schemaService.getSchema(name);
//    }

    @GetMapping("/menu")
    public List<MenuItemVO> getMenuSchema() {
        return schemaService.getMenuSchema();
    }
}
