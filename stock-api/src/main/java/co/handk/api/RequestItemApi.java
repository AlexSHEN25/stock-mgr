package co.handk.api;
import co.handk.common.model.vo.RequestItemVO;
import co.handk.common.model.dto.create.CreateRequestItemDTO;
import co.handk.common.model.dto.update.UpdateRequestItemDTO;
import co.handk.common.model.dto.query.RequestItemQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@Validated
@RequestMapping("/requestItem")
public interface RequestItemApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateRequestItemDTO dto);
    @GetMapping("/{id}")
    RequestItemVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateRequestItemDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<RequestItemVO> page(@Valid RequestItemQueryDTO query);
}
