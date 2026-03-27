package co.handk.backend.controller;

import co.handk.backend.entity.Warehouse;
import co.handk.backend.service.WarehouseService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @PostMapping
    public Boolean create(@RequestBody Warehouse entity) {
        return warehouseService.create(entity);
    }

    @GetMapping("/{id}")
    public Warehouse get(@PathVariable Long id) {
        return warehouseService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody Warehouse entity) {
        return warehouseService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return warehouseService.delete(id);
    }

    @GetMapping("/list")
    public List<Warehouse> list() {
        return warehouseService.listAll();
    }

    @GetMapping("/page")
    public PageResult<Warehouse> page(PageQuery query) {
        return warehouseService.pageQuery(query);
    }
}
