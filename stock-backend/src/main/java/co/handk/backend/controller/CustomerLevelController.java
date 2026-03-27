package co.handk.backend.controller;

import co.handk.backend.entity.CustomerLevel;
import co.handk.common.model.dto.CustomerLevelDTO;
import co.handk.backend.service.CustomerLevelService;
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
@RequestMapping("/customerLevel")
public class CustomerLevelController {

    @Autowired
    private CustomerLevelService customerLevelService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CustomerLevelDTO dto) {
        return customerLevelService.create(dto);
    }

    @GetMapping("/{id}")
    public CustomerLevel get(@PathVariable @NotNull Long id) {
        return customerLevelService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid CustomerLevelDTO dto) {
        return customerLevelService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return customerLevelService.delete(id);
    }

    @GetMapping("/list")
    public List<CustomerLevel> list() {
        return customerLevelService.listAll();
    }

    @GetMapping("/page")
    public PageResult<CustomerLevel> page(@Valid PageQuery query) {
        return customerLevelService.pageQuery(query);
    }
}
