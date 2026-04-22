package co.handk.backend.controller;

import co.handk.backend.service.CategoryService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateCategoryDTO;
import co.handk.common.model.dto.query.CategoryQueryDTO;
import co.handk.common.model.dto.update.UpdateCategoryDTO;
import co.handk.common.model.vo.CategoryVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateCategoryDTO dto) {
        return categoryService.create(dto);
    }

    @GetMapping("/{id}")
    public CategoryVO get(@PathVariable @NotNull Long id) {
        return categoryService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateCategoryDTO dto) {
        return categoryService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return categoryService.delete(id);
    }

    @GetMapping("/page")
    public PageResult<CategoryVO> page(@Valid CategoryQueryDTO query) {
        return categoryService.pageQuery(query);
    }
}
