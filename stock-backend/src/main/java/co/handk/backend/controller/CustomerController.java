package co.handk.backend.controller;

import co.handk.common.model.vo.CustomerVO;
import co.handk.common.model.dto.create.CreateCustomerDTO;
import co.handk.common.model.dto.update.UpdateCustomerDTO;
import co.handk.backend.service.CustomerService;
import co.handk.common.model.dto.query.CustomerQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@RestController
@Validated
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    private CustomerService customerService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateCustomerDTO dto) {
        return customerService.create(dto);
    }
    @GetMapping("/{id}")
    public CustomerVO get(@PathVariable @NotNull Long id) {
        return customerService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateCustomerDTO dto) {
        return customerService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return customerService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<CustomerVO> page(@Valid CustomerQueryDTO query) {
        return customerService.pageQuery(query);
    }
}
