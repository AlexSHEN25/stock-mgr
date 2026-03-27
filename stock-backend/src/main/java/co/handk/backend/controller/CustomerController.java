package co.handk.backend.controller;

import co.handk.backend.entity.Customer;
import co.handk.backend.service.CustomerService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public Boolean create(@RequestBody Customer entity) {
        return customerService.create(entity);
    }

    @GetMapping("/{id}")
    public Customer get(@PathVariable Long id) {
        return customerService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody Customer entity) {
        return customerService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return customerService.delete(id);
    }

    @GetMapping("/list")
    public List<Customer> list() {
        return customerService.listAll();
    }

    @GetMapping("/page")
    public PageResult<Customer> page(PageQuery query) {
        return customerService.pageQuery(query);
    }
}
