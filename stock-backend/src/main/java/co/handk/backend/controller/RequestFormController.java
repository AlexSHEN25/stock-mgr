package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.RequestFormService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateRequestFromOutboundDTO;
import co.handk.common.model.dto.create.CreateRequestFormDTO;
import co.handk.common.model.dto.query.RequestFormQueryDTO;
import co.handk.common.model.dto.update.UpdateRequestFormDTO;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/requestForm")
public class RequestFormController {
    @Autowired
    private RequestFormService requestFormService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateRequestFormDTO dto) {
        return requestFormService.saveByDto(dto);
    }

    @PostMapping("/fromOutbound")
    public Long createFromOutbound(@RequestBody @NotNull @Valid CreateRequestFromOutboundDTO dto) {
        return requestFormService.createFromOutbound(dto);
    }

    @PostMapping("/reapplyInbound/{id}")
    public Long reapplyInbound(@PathVariable("id") @NotNull Long id) {
        return requestFormService.reapplyInbound(id);
    }

    @GetMapping("/{id}")
    public RequestFormVO get(@PathVariable("id") @NotNull Long id) {
        return requestFormService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateRequestFormDTO dto) {
        return requestFormService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return requestFormService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return requestFormService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<RequestFormVO> page(@Valid RequestFormQueryDTO query) {
        return requestFormService.page(query);
    }
}


