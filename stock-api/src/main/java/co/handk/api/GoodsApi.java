package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsDTO;
import co.handk.common.model.dto.query.GoodsQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsDTO;
import co.handk.common.model.vo.GoodsVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/goods")
public interface GoodsApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateGoodsDTO dto);
    @GetMapping("/{id}")
    GoodsVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateGoodsDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<GoodsVO> page(@Valid GoodsQueryDTO query);
}
