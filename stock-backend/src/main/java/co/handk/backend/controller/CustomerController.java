package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.CustomerService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateCustomerDTO;
import co.handk.common.model.dto.query.CustomerQueryDTO;
import co.handk.common.model.dto.update.UpdateCustomerDTO;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateCustomerDTO dto) {
        return customerService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public CustomerVO get(@PathVariable("id") @NotNull Long id) {
        return customerService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateCustomerDTO dto) {
        return customerService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return customerService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return customerService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<CustomerVO> page(@Valid CustomerQueryDTO query) {
        return customerService.page(query);
    }
}


