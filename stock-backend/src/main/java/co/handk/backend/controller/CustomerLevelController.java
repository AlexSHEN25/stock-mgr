package co.handk.backend.controller;

import co.handk.backend.entity.CustomerLevel;
import co.handk.backend.service.CustomerLevelService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customerLevel")
public class CustomerLevelController {

    @Autowired
    private CustomerLevelService customerLevelService;

    @PostMapping
    public Boolean create(@RequestBody CustomerLevel entity) {
        return customerLevelService.create(entity);
    }

    @GetMapping("/{id}")
    public CustomerLevel get(@PathVariable Long id) {
        return customerLevelService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody CustomerLevel entity) {
        return customerLevelService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return customerLevelService.delete(id);
    }

    @GetMapping("/list")
    public List<CustomerLevel> list() {
        return customerLevelService.listAll();
    }

    @GetMapping("/page")
    public PageResult<CustomerLevel> page(PageQuery query) {
        return customerLevelService.pageQuery(query);
    }
}
