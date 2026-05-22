package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.BrandMakerRelationService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateBrandMakerRelationDTO;
import co.handk.common.model.dto.query.BrandMakerRelationQueryDTO;
import co.handk.common.model.dto.update.UpdateBrandMakerRelationDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/brandMakerRelation")
@RequiredArgsConstructor
public class BrandMakerRelationController {
    private final BrandMakerRelationService brandMakerRelationService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateBrandMakerRelationDTO dto) {
        return brandMakerRelationService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public BrandMakerRelationVO get(@PathVariable("id") @NotNull Long id) {
        return brandMakerRelationService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateBrandMakerRelationDTO dto) {
        return brandMakerRelationService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return brandMakerRelationService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return brandMakerRelationService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<BrandMakerRelationVO> page(@Valid BrandMakerRelationQueryDTO query) {
        return brandMakerRelationService.page(query);
    }
}

