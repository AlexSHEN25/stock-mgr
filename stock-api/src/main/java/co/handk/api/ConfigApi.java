package co.handk.api;
import co.handk.common.model.vo.ConfigVO;
import co.handk.common.model.dto.create.CreateConfigDTO;
import co.handk.common.model.dto.update.UpdateConfigDTO;
import co.handk.common.model.dto.query.ConfigQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@Validated
@RequestMapping("/config")
public interface ConfigApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateConfigDTO dto);
    @GetMapping("/{id}")
    ConfigVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateConfigDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<ConfigVO> page(@Valid ConfigQueryDTO query);
}
