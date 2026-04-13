package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsLevelPriceDTO;
import co.handk.common.model.dto.query.GoodsLevelPriceQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsLevelPriceDTO;
import co.handk.common.model.vo.GoodsLevelPriceVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/goodsLevelPrice")
public interface GoodsLevelPriceApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateGoodsLevelPriceDTO dto);

    @GetMapping("/{id}")
    GoodsLevelPriceVO get(@PathVariable @NotNull Long id);

    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateGoodsLevelPriceDTO dto);

    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);

    @GetMapping("/page")
    PageResult<GoodsLevelPriceVO> page(@Valid GoodsLevelPriceQueryDTO query);
}
