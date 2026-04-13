package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsImageDTO;
import co.handk.common.model.dto.query.GoodsImageQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsImageDTO;
import co.handk.common.model.vo.GoodsImageVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/goodsImage")
public interface GoodsImageApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateGoodsImageDTO dto);

    @GetMapping("/{id}")
    GoodsImageVO get(@PathVariable @NotNull Long id);

    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateGoodsImageDTO dto);

    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);

    @GetMapping("/page")
    PageResult<GoodsImageVO> page(@Valid GoodsImageQueryDTO query);
}
