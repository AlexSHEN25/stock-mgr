package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.WarehouseService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateWarehouseDTO;
import co.handk.common.model.dto.query.WarehouseQueryDTO;
import co.handk.common.model.dto.update.UpdateWarehouseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/warehouse")
@RequiredArgsConstructor
public class WarehouseController {
    private final WarehouseService warehouseService;

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
        return warehouseService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return warehouseService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<WarehouseVO> page(@Valid WarehouseQueryDTO query) {
        return warehouseService.page(query);
    }
}

