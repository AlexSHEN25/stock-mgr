package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateCategoryDTO;
import co.handk.common.model.dto.query.CategoryQueryDTO;
import co.handk.common.model.dto.update.UpdateCategoryDTO;
import co.handk.common.model.vo.CategoryVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/category")
public interface CategoryApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateCategoryDTO dto);

    @GetMapping("/{id}")
    CategoryVO get(@PathVariable @NotNull Long id);

    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateCategoryDTO dto);

    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);

    @GetMapping("/page")
    PageResult<CategoryVO> page(@Valid CategoryQueryDTO query);
}
