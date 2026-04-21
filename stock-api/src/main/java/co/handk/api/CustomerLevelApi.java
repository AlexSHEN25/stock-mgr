package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateCustomerLevelDTO;
import co.handk.common.model.dto.query.CustomerLevelQueryDTO;
import co.handk.common.model.dto.update.UpdateCustomerLevelDTO;
import co.handk.common.model.vo.CustomerLevelVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/customerLevel")
public interface CustomerLevelApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateCustomerLevelDTO dto);
    @GetMapping("/{id}")
    CustomerLevelVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateCustomerLevelDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<CustomerLevelVO> page(@Valid CustomerLevelQueryDTO query);
}
