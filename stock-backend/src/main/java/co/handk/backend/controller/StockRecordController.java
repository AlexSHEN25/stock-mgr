package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.StockRecordService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.StockRecordVO;
import co.handk.common.model.dto.query.StockRecordQueryDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/stockRecord")
@RequiredArgsConstructor
    public class StockRecordController {
    private final StockRecordService stockRecordService;

    @GetMapping("/{id}")
    public StockRecordVO get(@PathVariable("id") @NotNull Long id) {
        return stockRecordService.getVOById(id);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return stockRecordService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return stockRecordService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<StockRecordVO> page(@Valid StockRecordQueryDTO query) {
        return stockRecordService.page(query);
    }
}

