package co.handk.backend.controller;

import co.handk.common.model.vo.WarehouseVO;
import co.handk.common.model.dto.create.CreateWarehouseDTO;
import co.handk.common.model.dto.update.UpdateWarehouseDTO;
import co.handk.backend.service.WarehouseService;
import co.handk.common.model.dto.query.WarehouseQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@RestController
@Validated
@RequestMapping("/warehouse")
public class WarehouseController {
    @Autowired
    private WarehouseService warehouseService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateWarehouseDTO dto) {
        return warehouseService.create(dto);
    }
    @GetMapping("/{id}")
    public WarehouseVO get(@PathVariable @NotNull Long id) {
        return warehouseService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateWarehouseDTO dto) {
        return warehouseService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return warehouseService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<WarehouseVO> page(@Valid WarehouseQueryDTO query) {
        return warehouseService.pageQuery(query);
    }
}
