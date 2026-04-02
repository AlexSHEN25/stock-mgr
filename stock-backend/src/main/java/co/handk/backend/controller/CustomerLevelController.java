package co.handk.backend.controller;
import co.handk.common.model.vo.CustomerLevelVO;
import co.handk.common.model.dto.create.CreateCustomerLevelDTO;
import co.handk.common.model.dto.update.UpdateCustomerLevelDTO;
import co.handk.backend.service.CustomerLevelService;
import co.handk.common.model.dto.query.CustomerLevelQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@RestController
@Validated
@RequestMapping("/customerLevel")
public class CustomerLevelController {
    @Autowired
    private CustomerLevelService customerLevelService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateCustomerLevelDTO dto) {
        return customerLevelService.create(dto);
    }
    @GetMapping("/{id}")
    public CustomerLevelVO get(@PathVariable @NotNull Long id) {
        return customerLevelService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateCustomerLevelDTO dto) {
        return customerLevelService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return customerLevelService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<CustomerLevelVO> page(@Valid CustomerLevelQueryDTO query) {
        return customerLevelService.pageQuery(query);
    }
}
