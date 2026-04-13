package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsSkuSpecDTO;
import co.handk.common.model.dto.query.GoodsSkuSpecQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsSkuSpecDTO;
import co.handk.common.model.vo.GoodsSkuSpecVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/goodsSkuSpec")
public interface GoodsSkuSpecApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateGoodsSkuSpecDTO dto);

    @GetMapping("/{id}")
    GoodsSkuSpecVO get(@PathVariable @NotNull Long id);

    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateGoodsSkuSpecDTO dto);

    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);

    @GetMapping("/page")
    PageResult<GoodsSkuSpecVO> page(@Valid GoodsSkuSpecQueryDTO query);
}
