package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.CategoryService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateCategoryDTO;
import co.handk.common.model.dto.query.CategoryQueryDTO;
import co.handk.common.model.dto.update.UpdateCategoryDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateCategoryDTO dto) {
        return categoryService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public CategoryVO get(@PathVariable("id") @NotNull Long id) {
        return categoryService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateCategoryDTO dto) {
        return categoryService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return categoryService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return categoryService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<CategoryVO> page(@Valid CategoryQueryDTO query) {
        return categoryService.page(query);
    }
}

