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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/stock")
public class StockController {

    private final StockService stockService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateStockDTO dto) {
        throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "在庫の直接作成はできません。入庫処理を利用してください。");
    }

    @PostMapping({"/self", "/handle"})
    public Boolean createScoped(@RequestBody @NotNull @Valid CreateStockDTO dto) {
        return create(dto);
    }

    @GetMapping("/{id}")
    public StockVO get(@PathVariable("id") @NotNull Long id) {
        return stockService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateStockDTO dto) {
        return stockService.updateByDto(dto);
    }

    @PutMapping("/self")
    public Boolean updateSelf(@RequestBody @NotNull @Valid UpdateStockDTO dto) {
        return stockService.updateSelfStock(dto);
    }

    @PutMapping("/handle")
    public Boolean updateHandle(@RequestBody @NotNull @Valid UpdateStockDTO dto) {
        return stockService.updateHandleStock(dto);
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
        return stockService.approveOrder(orderId, approved, remark);
    }

    @PostMapping("/approve/{orderId}")
    public Boolean approveOrder(@PathVariable("orderId") @NotNull Long orderId,
                                @RequestParam("approved") @NotNull Boolean approved,
                                @RequestParam(value = "remark", required = false) String remark) {
        return stockService.approveOrder(orderId, approved, remark);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return stockService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return stockService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/self/{id}")
    public StockVO getSelf(@PathVariable("id") @NotNull Long id) {
        return stockService.getSelfStockById(id);
    }

    @GetMapping("/handle/{id}")
    public StockVO getHandle(@PathVariable("id") @NotNull Long id) {
        return stockService.getHandleStockById(id);
    }

    @DeleteMapping("/self/{id}")
    public Boolean deleteSelf(@PathVariable("id") @NotNull Long id) {
        return stockService.deleteSelfStockById(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/handle/{id}")
    public Boolean deleteHandle(@PathVariable("id") @NotNull Long id) {
        return stockService.deleteHandleStockById(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/self/batch")
    public Boolean deleteSelfBatch(@RequestBody @NotNull List<Long> ids) {
        return stockService.deleteSelfStockBatch(ids) > NumberConstant.ZERO;
    }

    @DeleteMapping("/handle/batch")
    public Boolean deleteHandleBatch(@RequestBody @NotNull List<Long> ids) {
        return stockService.deleteHandleStockBatch(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<StockVO> page(@Valid StockQueryDTO query) {
        return stockService.page(query);
    }

    @GetMapping("/self/page")
    public PageResult<StockVO> pageSelf(@Valid StockQueryDTO query) {
        return stockService.pageSelfStock(query);
    }

    @GetMapping("/handle/page")
    public PageResult<StockVO> pageHandle(@Valid StockQueryDTO query) {
        return stockService.pageHandleStock(query);
    }
}
