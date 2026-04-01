package co.handk.backend.controller;

import co.handk.backend.entity.Brand;
import co.handk.common.model.vo.BrandVO;
import co.handk.common.model.dto.BrandDTO;
import co.handk.backend.service.BrandService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@Validated
@RequestMapping("/brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid BrandDTO dto) {
        return brandService.create(dto);
    }

    @GetMapping("/{id}")
    public BrandVO get(@PathVariable @NotNull Long id) {
        return brandService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid BrandDTO dto) {
        return brandService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return brandService.delete(id);
    }

    @GetMapping("/list")
    public List<BrandVO> list() {
        return brandService.listAll();
    }

    @GetMapping("/page")
    public PageResult<BrandVO> page(@Valid PageQuery query) {
        return brandService.pageQuery(query);
    }
}
