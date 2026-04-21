package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateMakerDTO;
import co.handk.common.model.dto.query.MakerQueryDTO;
import co.handk.common.model.dto.update.UpdateMakerDTO;
import co.handk.common.model.vo.MakerVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/maker")
public interface MakerApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateMakerDTO dto);
    @GetMapping("/{id}")
    MakerVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateMakerDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<MakerVO> page(@Valid MakerQueryDTO query);
}
