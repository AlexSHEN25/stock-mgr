package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateBrandDTO;
import co.handk.common.model.dto.query.BrandQueryDTO;
import co.handk.common.model.dto.update.UpdateBrandDTO;
import co.handk.common.model.vo.BrandVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/brand")
public interface BrandApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateBrandDTO dto);
    @GetMapping("/{id}")
    BrandVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateBrandDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<BrandVO> page(@Valid BrandQueryDTO query);
}
