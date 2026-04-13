package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateBrandMakerRelationDTO;
import co.handk.common.model.dto.query.BrandMakerRelationQueryDTO;
import co.handk.common.model.dto.update.UpdateBrandMakerRelationDTO;
import co.handk.common.model.vo.BrandMakerRelationVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/brandMakerRelation")
public interface BrandMakerRelationApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateBrandMakerRelationDTO dto);

    @GetMapping("/{id}")
    BrandMakerRelationVO get(@PathVariable @NotNull Long id);

    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateBrandMakerRelationDTO dto);

    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);

    @GetMapping("/page")
    PageResult<BrandMakerRelationVO> page(@Valid BrandMakerRelationQueryDTO query);
}
