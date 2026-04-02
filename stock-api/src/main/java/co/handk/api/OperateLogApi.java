package co.handk.api;
import co.handk.common.model.vo.OperateLogVO;
import co.handk.common.model.dto.create.CreateOperateLogDTO;
import co.handk.common.model.dto.update.UpdateOperateLogDTO;
import co.handk.common.model.dto.query.OperateLogQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@Validated
@RequestMapping("/operateLog")
public interface OperateLogApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateOperateLogDTO dto);
    @GetMapping("/{id}")
    OperateLogVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateOperateLogDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<OperateLogVO> page(@Valid OperateLogQueryDTO query);
}
