package co.handk.backend.controller;

import co.handk.backend.service.StockService;
import co.handk.backend.exception.BusinessException;
import co.handk.common.constant.NumberConstant;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.StockOperateDTO;
import co.handk.common.model.dto.create.StockOrderSubmitDTO;
import co.handk.common.model.dto.query.CustomerStockQueryDTO;
import co.handk.common.model.dto.query.StockQueryDTO;
import co.handk.common.model.dto.update.UpdateStockDTO;
import co.handk.common.model.vo.StockVO;
import co.handk.common.model.vo.CustomerGoodsStockDetailVO;
import co.handk.common.model.vo.CustomerGoodsStockVO;
import co.handk.common.model.vo.CustomerStockSummaryVO;
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

    @GetMapping("/{id}")
    public StockVO get(@PathVariable("id") @NotNull Long id) {
        return stockService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateStockDTO dto) {
        return stockService.updateByDto(dto);
    }

    @PostMapping("/inbound")
    public Long inbound(@RequestBody @NotNull @Valid StockOperateDTO dto) {
        return stockService.inbound(dto);
    }

    @PostMapping("/outbound")
    public Long outbound(@RequestBody @NotNull @Valid StockOperateDTO dto) {
        return stockService.outbound(dto);
    }

    /**
     * Customer-dimension outbound entry from self stock / goods management.
     */
    @PostMapping("/customer/outbound")
    public Long customerOutbound(@RequestBody @NotNull @Valid StockOperateDTO dto) {
        dto.setOutboundMode(co.handk.common.constant.StockBizConstant.OUTBOUND_MODE_CUSTOMER);
        if (dto.getCustomerId() == null) {
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "customerId is required");
        }
        return stockService.outbound(dto);
    }

    /**
     * Customer-dimension outbound entry from group stock.
     */
    @PostMapping("/group/customer/outbound")
    public Long groupCustomerOutbound(@RequestBody @NotNull @Valid StockOperateDTO dto) {
        if (dto.getCustomerId() == null) {
            // Backward compatibility: the web client previously sent group allocation
            // requests to this endpoint. A target group without a customer is allocation.
            dto.setOutboundMode(co.handk.common.constant.StockBizConstant.OUTBOUND_MODE_GROUP_ALLOCATE);
            return stockService.outbound(dto);
        }
        dto.setOutboundMode(co.handk.common.constant.StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER);
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

    @GetMapping("/page")
    public PageResult<StockVO> page(@Valid StockQueryDTO query) {
        return stockService.page(query);
    }

    @GetMapping("/group/available")
    public Integer groupAvailable(@RequestParam("goodsId") Long goodsId,
                                  @RequestParam("skuId") Long skuId,
                                  @RequestParam("warehouseId") Long warehouseId,
                                  @RequestParam(value = "stockTypeId", required = false) Long stockTypeId) {
        return stockService.getMyGroupAvailableQty(goodsId, skuId, warehouseId, stockTypeId);
    }

    @GetMapping("/customer/page")
    public PageResult<CustomerStockSummaryVO> customerPage(@Valid CustomerStockQueryDTO query) {
        return stockService.pageCustomerStock(query);
    }

    @GetMapping("/customer/goods/page")
    public PageResult<CustomerGoodsStockVO> customerGoodsPage(@Valid CustomerStockQueryDTO query) {
        return stockService.pageCustomerGoodsStock(query);
    }

    @GetMapping("/customer/goods/detail/page")
    public PageResult<CustomerGoodsStockDetailVO> customerGoodsDetailPage(@Valid CustomerStockQueryDTO query) {
        return stockService.pageCustomerGoodsStockDetails(query);
    }
}
