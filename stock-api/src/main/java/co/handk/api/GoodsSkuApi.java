package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsSkuDTO;
import co.handk.common.model.dto.query.GoodsSkuQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsSkuDTO;
import co.handk.common.model.vo.GoodsSkuVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/goodsSku")
public interface GoodsSkuApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateGoodsSkuDTO dto);

    @GetMapping("/{id}")
    GoodsSkuVO get(@PathVariable @NotNull Long id);

    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateGoodsSkuDTO dto);

    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);

    @GetMapping("/page")
    PageResult<GoodsSkuVO> page(@Valid GoodsSkuQueryDTO query);
}
