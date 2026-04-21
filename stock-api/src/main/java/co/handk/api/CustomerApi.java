package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateCustomerDTO;
import co.handk.common.model.dto.query.CustomerQueryDTO;
import co.handk.common.model.dto.update.UpdateCustomerDTO;
import co.handk.common.model.vo.CustomerVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/customer")
public interface CustomerApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateCustomerDTO dto);
    @GetMapping("/{id}")
    CustomerVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateCustomerDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<CustomerVO> page(@Valid CustomerQueryDTO query);
}
