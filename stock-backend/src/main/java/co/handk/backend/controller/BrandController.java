package co.handk.backend.controller;

import co.handk.backend.service.BrandService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateBrandDTO;
import co.handk.common.model.dto.query.BrandQueryDTO;
import co.handk.common.model.dto.update.UpdateBrandDTO;
import co.handk.common.model.vo.BrandVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;
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
        return brandService.deleteByIdLogic(id) > 0;
    }
    @GetMapping("/page")
    public PageResult<BrandVO> page(@Valid BrandQueryDTO query) {
        return brandService.page(query);
    }
}

