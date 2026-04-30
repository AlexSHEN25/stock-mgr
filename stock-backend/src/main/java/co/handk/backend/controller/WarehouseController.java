package co.handk.backend.controller;

import co.handk.backend.service.WarehouseService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateWarehouseDTO;
import co.handk.common.model.dto.query.WarehouseQueryDTO;
import co.handk.common.model.dto.update.UpdateWarehouseDTO;
import co.handk.common.model.vo.WarehouseVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/warehouse")
public class WarehouseController {
    @Autowired
    private WarehouseService warehouseService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateWarehouseDTO dto) {
        return warehouseService.saveByDto(dto);
    }
    @GetMapping("/{id}")
    public WarehouseVO get(@PathVariable("id") @NotNull Long id) {
        return warehouseService.getVOById(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateWarehouseDTO dto) {
        return warehouseService.updateByDto(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return warehouseService.deleteByIdLogic(id) > 0;
    }
    @GetMapping("/page")
    public PageResult<WarehouseVO> page(@Valid WarehouseQueryDTO query) {
        return warehouseService.page(query);
    }
}

