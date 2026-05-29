package co.handk.backend.controller;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.service.StockService;
import co.handk.common.constant.NumberConstant;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateStockDTO;
import co.handk.common.model.dto.create.StockOrderSubmitDTO;
import co.handk.common.model.dto.create.StockOperateDTO;
import co.handk.common.model.dto.query.StockQueryDTO;
import co.handk.common.model.dto.update.UpdateStockDTO;
import co.handk.common.model.vo.StockVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/stock")
public class StockController {

    private final StockService stockService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateStockDTO dto) {
        throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "在庫の手動追加はできません。在庫入庫を実行してください。");
    }

    @GetMapping("/{id}")
    public StockVO get(@PathVariable("id") @NotNull Long id) {
        return stockService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateStockDTO dto) {
        throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "在庫の直接更新はできません。入出庫処理を実行してください。");
    }

    @PostMapping("/inbound")
    public Long inbound(@RequestBody @NotNull @Valid StockOperateDTO dto) {
        return stockService.inbound(dto);
    }

    @PostMapping("/outbound")
    public Long outbound(@RequestBody @NotNull @Valid StockOperateDTO dto) {
        return stockService.outbound(dto);
    }

    @PostMapping("/submit")
    public Long submit(@RequestBody @NotNull @Valid StockOrderSubmitDTO dto) {
        return stockService.submitOrder(dto);
    }

    @PostMapping("/inbound/approve/{orderId}")
    public Boolean approveInbound(@PathVariable("orderId") @NotNull Long orderId,
                                  @RequestParam("approved") @NotNull Boolean approved,
                                  @RequestParam(value = "remark", required = false) String remark) {
        return stockService.approveInbound(orderId, approved, remark);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return stockService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return stockService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<StockVO> page(@Valid StockQueryDTO query) {
        return stockService.page(query);
    }
}
