package co.handk.backend.controller;

import co.handk.backend.entity.Customer;
import co.handk.common.model.dto.CustomerDTO;
import co.handk.backend.service.CustomerService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@Validated
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CustomerDTO dto) {
        return customerService.create(dto);
    }

    @GetMapping("/{id}")
    public Customer get(@PathVariable @NotNull Long id) {
        return customerService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid CustomerDTO dto) {
        return customerService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return customerService.delete(id);
    }

    @GetMapping("/list")
    public List<Customer> list() {
        return customerService.listAll();
    }

    @GetMapping("/page")
    public PageResult<Customer> page(@Valid PageQuery query) {
        return customerService.pageQuery(query);
    }
}
