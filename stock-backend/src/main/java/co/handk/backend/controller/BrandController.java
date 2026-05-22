package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.BrandService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateBrandDTO;
import co.handk.common.model.dto.query.BrandQueryDTO;
import co.handk.common.model.dto.update.UpdateBrandDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/brand")
@RequiredArgsConstructor
public class BrandController {
    private final BrandService brandService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateBrandDTO dto) {
        return brandService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public BrandVO get(@PathVariable("id") @NotNull Long id) {
        return brandService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateBrandDTO dto) {
        return brandService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return brandService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return brandService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<BrandVO> page(@Valid BrandQueryDTO query) {
        return brandService.page(query);
    }
}

