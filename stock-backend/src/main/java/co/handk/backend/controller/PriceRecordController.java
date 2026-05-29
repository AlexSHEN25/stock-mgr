package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.PriceRecordService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.PriceRecordVO;
import co.handk.common.model.dto.query.PriceRecordQueryDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/priceRecord")
@RequiredArgsConstructor
public class PriceRecordController {
    private final PriceRecordService priceRecordService;

    @GetMapping("/{id}")
    public PriceRecordVO get(@PathVariable("id") @NotNull Long id) {
        return priceRecordService.getVOById(id);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return priceRecordService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return priceRecordService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<PriceRecordVO> page(@Valid PriceRecordQueryDTO query) {
        return priceRecordService.page(query);
    }
}

