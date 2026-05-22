package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.RequestItemService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateRequestItemDTO;
import co.handk.common.model.dto.query.RequestItemQueryDTO;
import co.handk.common.model.dto.update.UpdateRequestItemDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/requestItem")
@RequiredArgsConstructor
public class RequestItemController {
    private final RequestItemService requestItemService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateRequestItemDTO dto) {
        return requestItemService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public RequestItemVO get(@PathVariable("id") @NotNull Long id) {
        return requestItemService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateRequestItemDTO dto) {
        return requestItemService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return requestItemService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return requestItemService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<RequestItemVO> page(@Valid RequestItemQueryDTO query) {
        return requestItemService.page(query);
    }
}

