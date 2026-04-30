package co.handk.backend.controller;

import co.handk.backend.service.CustomerLevelService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateCustomerLevelDTO;
import co.handk.common.model.dto.query.CustomerLevelQueryDTO;
import co.handk.common.model.dto.update.UpdateCustomerLevelDTO;
import co.handk.common.model.vo.CustomerLevelVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/customerLevel")
public class CustomerLevelController {
    @Autowired
    private CustomerLevelService customerLevelService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateCustomerLevelDTO dto) {
        return customerLevelService.saveByDto(dto);
    }
    @GetMapping("/{id}")
    public CustomerLevelVO get(@PathVariable("id") @NotNull Long id) {
        return customerLevelService.getVOById(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateCustomerLevelDTO dto) {
        return customerLevelService.updateByDto(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return customerLevelService.deleteByIdLogic(id) > 0;
    }
    @GetMapping("/page")
    public PageResult<CustomerLevelVO> page(@Valid CustomerLevelQueryDTO query) {
        return customerLevelService.page(query);
    }
}

