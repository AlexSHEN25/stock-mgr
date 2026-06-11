package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.StockOrderService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateStockOrderDTO;
import co.handk.common.model.dto.query.StockOrderQueryDTO;
import co.handk.common.model.dto.update.UpdateStockOrderDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/stockOrder")
public class StockOrderController {
    private final StockOrderService stockOrderService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateStockOrderDTO dto) {
        return stockOrderService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public StockOrderVO get(@PathVariable("id") @NotNull Long id) {
        return stockOrderService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateStockOrderDTO dto) {
        return stockOrderService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return stockOrderService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return stockOrderService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<StockOrderVO> page(@Valid StockOrderQueryDTO query) {
        return stockOrderService.page(query);
    }

    /**
     * Customer-dimension order query alias.
     */
    @GetMapping("/customer/page")
    public PageResult<StockOrderVO> customerPage(@Valid StockOrderQueryDTO query) {
        if (query.getOutboundMode() == null || query.getOutboundMode().isBlank()) {
            query.setOutboundMode(co.handk.common.constant.StockBizConstant.OUTBOUND_MODE_CUSTOMER);
        }
        return stockOrderService.page(query);
    }

    /**
     * Customer-dimension order detail alias.
     */
    @GetMapping("/customer/{id}")
    public StockOrderVO customerGet(@PathVariable("id") @NotNull Long id) {
        return stockOrderService.getVOById(id);
    }

    /**
     * Customer-dimension order update alias.
     */
    @PutMapping("/customer")
    public Boolean customerUpdate(@RequestBody @NotNull @Valid UpdateStockOrderDTO dto) {
        return stockOrderService.updateByDto(dto);
    }
}

