package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateRequestFormDTO;
import co.handk.common.model.dto.query.RequestFormQueryDTO;
import co.handk.common.model.dto.update.UpdateRequestFormDTO;
import co.handk.common.model.vo.RequestFormVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/requestForm")
public interface RequestFormApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateRequestFormDTO dto);
    @GetMapping("/{id}")
    RequestFormVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateRequestFormDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<RequestFormVO> page(@Valid RequestFormQueryDTO query);
}
