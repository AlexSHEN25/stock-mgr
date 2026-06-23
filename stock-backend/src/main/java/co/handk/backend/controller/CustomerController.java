package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.CustomerService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateCustomerDTO;
import co.handk.common.model.dto.query.CustomerQueryDTO;
import co.handk.common.model.dto.update.UpdateCustomerDTO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Validated
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

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

    @GetMapping("/export")
    public void export(@ModelAttribute CustomerQueryDTO query, HttpServletResponse response) {
        customerService.exportCustomers(query, response);
    }

    @GetMapping("/import/template")
    public void downloadImportTemplate(HttpServletResponse response) {
        customerService.downloadImportTemplate(response);
    }

    @PostMapping("/import/upsert")
    public CustomerImportResultVO importUpsert(@RequestPart("file") MultipartFile file) {
        return customerService.importCustomers(file);
    }
}

