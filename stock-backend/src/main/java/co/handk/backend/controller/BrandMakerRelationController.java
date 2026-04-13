package co.handk.backend.controller;

import co.handk.api.BrandMakerRelationApi;
import co.handk.backend.service.BrandMakerRelationService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateBrandMakerRelationDTO;
import co.handk.common.model.dto.query.BrandMakerRelationQueryDTO;
import co.handk.common.model.dto.update.UpdateBrandMakerRelationDTO;
import co.handk.common.model.vo.BrandMakerRelationVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/brandMakerRelation")
public class BrandMakerRelationController implements BrandMakerRelationApi {
    @Autowired
    private BrandMakerRelationService brandMakerRelationService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateBrandMakerRelationDTO dto) {
        return brandMakerRelationService.create(dto);
    }

    @GetMapping("/{id}")
    public BrandMakerRelationVO get(@PathVariable @NotNull Long id) {
        return brandMakerRelationService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateBrandMakerRelationDTO dto) {
        return brandMakerRelationService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return brandMakerRelationService.delete(id);
    }

    @GetMapping("/page")
    public PageResult<BrandMakerRelationVO> page(@Valid BrandMakerRelationQueryDTO query) {
        return brandMakerRelationService.pageQuery(query);
    }
}
