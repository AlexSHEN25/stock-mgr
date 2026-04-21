package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsTypeDTO;
import co.handk.common.model.dto.query.GoodsTypeQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsTypeDTO;
import co.handk.common.model.vo.GoodsTypeVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/goodsType")
public interface GoodsTypeApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateGoodsTypeDTO dto);
    @GetMapping("/{id}")
    GoodsTypeVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateGoodsTypeDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<GoodsTypeVO> page(@Valid GoodsTypeQueryDTO query);
}
