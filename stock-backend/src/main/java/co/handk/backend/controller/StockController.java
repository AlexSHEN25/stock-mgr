package co.handk.backend.controller;

import co.handk.backend.service.StockService;
import co.handk.backend.service.StockBatchService;
import co.handk.backend.service.RequestFormService;
import co.handk.backend.exception.BusinessException;
import co.handk.common.constant.NumberConstant;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.StockOperateDTO;
import co.handk.common.model.dto.create.StockBatchOperateDTO;
import co.handk.common.model.dto.create.StockCustomerOutboundItemDTO;
import co.handk.common.model.dto.create.StockGroupAllocateDTO;
import co.handk.common.model.dto.create.StockOrderSubmitDTO;
import co.handk.common.model.dto.query.CustomerStockQueryDTO;
import co.handk.common.model.dto.query.StockQueryDTO;
import co.handk.common.model.dto.update.RequestFormItemBatchDTO;
import co.handk.common.model.dto.update.UpdateStockDTO;
import co.handk.common.model.vo.StockVO;
import co.handk.common.model.vo.CustomerGoodsStockDetailVO;
import co.handk.common.model.vo.CustomerGoodsStockVO;
import co.handk.common.model.vo.CustomerGoodsMatrixVO;
import co.handk.common.model.vo.CustomerOutboundTreeNodeVO;
import co.handk.common.model.vo.CustomerStockSummaryVO;
import co.handk.common.model.vo.StockBatchOptionVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/stock")
public class StockController {

    private final StockService stockService;
    private final StockBatchService stockBatchService;
    private final RequestFormService requestFormService;

    @GetMapping("/{id}")
    public StockVO get(@PathVariable("id") @NotNull Long id) {
        return stockService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateStockDTO dto) {
        return stockService.updateByDto(dto);
    }

    @PostMapping("/inbound")
    public Long inbound(@RequestBody @NotNull StockOperateDTO dto) {
        requirePositiveQuantity(dto.getQuantity());
        return stockService.inbound(dto);
    }

    @PostMapping("/inbound/batch")
    public Long batchInbound(@RequestBody @NotNull @Valid StockBatchOperateDTO dto) {
        return stockService.batchInbound(dto);
    }

    @PostMapping("/outbound")
    public Long outbound(@RequestBody @NotNull StockOperateDTO dto) {
        requirePositiveQuantity(dto.getQuantity());
        Long legacyAllocationOrderId = tryHandleLegacyGroupAllocation(dto);
        if (legacyAllocationOrderId != null) {
            return legacyAllocationOrderId;
        }
        return stockService.outbound(dto);
    }

    @PostMapping("/outbound/batch")
    public Long batchOutbound(@RequestBody @NotNull @Valid StockBatchOperateDTO dto) {
        return stockService.batchOutbound(dto);
    }

    @PostMapping("/inout/batch")
    public Long batchInOut(@RequestBody @NotNull @Valid StockBatchOperateDTO dto) {
        return stockService.batchInOut(dto);
    }

    /**
     * Customer-dimension outbound entry from self stock / goods management.
     */
    @PostMapping("/customer/outbound")
    @Transactional(rollbackFor = Exception.class)
    public Long customerOutbound(@RequestBody @NotNull StockOperateDTO dto) {
        if (hasCustomerSplitItems(dto)) {
            return splitCustomerOutbound(dto);
        }
        requirePositiveQuantity(dto.getQuantity());
        if (dto.getCustomerId() == null && hasGroupAllocationTarget(dto)) {
            Long legacyAllocationOrderId = tryHandleLegacyGroupAllocation(dto);
            if (legacyAllocationOrderId != null) {
                return legacyAllocationOrderId;
            }
            dto.setOutboundMode(co.handk.common.constant.StockBizConstant.OUTBOUND_MODE_GROUP_ALLOCATE);
            return stockService.outbound(dto);
        }
        dto.setOutboundMode(co.handk.common.constant.StockBizConstant.OUTBOUND_MODE_CUSTOMER);
        if (dto.getCustomerId() == null) {
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "顧客IDは必須です");
        }
        return stockService.outbound(dto);
    }

    /**
     * Customer-dimension outbound entry from group stock.
     */
    @PostMapping("/group/customer/outbound")
    public Long groupCustomerOutbound(@RequestBody @NotNull StockOperateDTO dto) {
        requirePositiveQuantity(dto.getQuantity());
        if (dto.getCustomerId() == null) {
            // Backward compatibility: the web client previously sent group allocation
            // requests to this endpoint. A target group without a customer is allocation.
            Long legacyAllocationOrderId = tryHandleLegacyGroupAllocation(dto);
            if (legacyAllocationOrderId != null) {
                return legacyAllocationOrderId;
            }
            dto.setOutboundMode(co.handk.common.constant.StockBizConstant.OUTBOUND_MODE_GROUP_ALLOCATE);
            return stockService.outbound(dto);
        }
        dto.setOutboundMode(co.handk.common.constant.StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER);
        return stockService.outbound(dto);
    }

    @PostMapping("/group/allocate")
    public List<Long> allocateToGroups(@RequestBody @NotNull @Valid StockGroupAllocateDTO dto) {
        return stockService.allocateToGroups(dto);
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

    @GetMapping("/self/export")
    public void exportSelf(@ModelAttribute StockQueryDTO query, HttpServletResponse response) {
        stockService.exportSelfStock(query, response);
    }

    @GetMapping("/group/available")
    public Integer groupAvailable(@RequestParam("goodsId") Long goodsId,
                                  @RequestParam("skuId") Long skuId,
                                  @RequestParam("warehouseId") Long warehouseId,
                                  @RequestParam(value = "stockTypeId", required = false) Long stockTypeId,
                                  @RequestParam(value = "deptId", required = false) Long deptId,
                                  @RequestParam(value = "groupCode", required = false) String groupCode) {
        return stockService.getGroupAvailableQty(
                goodsId, skuId, warehouseId, stockTypeId, deptId, groupCode);
    }

    @GetMapping("/{stockId}/batch-options")
    public List<StockBatchOptionVO> batchOptions(@PathVariable("stockId") @NotNull Long stockId,
                                                 @RequestParam(value = "deptId", required = false) Long deptId) {
        return stockBatchService.listOutboundBatchOptions(stockId, deptId);
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

    @GetMapping("/customer/delivery-schedule/page")
    public PageResult<CustomerGoodsStockDetailVO> customerDeliverySchedulePage(@Valid CustomerStockQueryDTO query) {
        query.setViewType("deliverySchedule");
        return stockService.pageCustomerDeliveryScheduleDetails(query);
    }

    @PostMapping("/customer/delivery-schedule/items/add-to-request")
    public Boolean addDeliveryScheduleItemsToRequest(@RequestBody @NotNull @Valid RequestFormItemBatchDTO dto) {
        return requestFormService.addItemsFromStockOrder(dto);
    }

    @PostMapping("/customer/delivery-schedule/items/return-from-request")
    public Boolean returnRequestItemsToDeliverySchedule(@RequestBody @NotNull @Valid RequestFormItemBatchDTO dto) {
        return requestFormService.removeItemsFromRequest(dto);
    }

    @GetMapping("/customer/goods/tree/page")
    public PageResult<CustomerOutboundTreeNodeVO> customerGoodsTreePage(@Valid CustomerStockQueryDTO query) {
        return stockService.pageCustomerGoodsTree(query);
    }

    @GetMapping("/customer/goods/matrix")
    public CustomerGoodsMatrixVO customerGoodsMatrix(@Valid CustomerStockQueryDTO query) {
        return stockService.getCustomerGoodsMatrix(query);
    }

    @GetMapping("/customer/delivery-schedule/matrix")
    public CustomerGoodsMatrixVO customerDeliveryScheduleMatrix(@Valid CustomerStockQueryDTO query) {
        query.setViewType("deliverySchedule");
        return stockService.getCustomerGoodsMatrix(query);
    }

    private boolean hasCustomerSplitItems(StockOperateDTO dto) {
        return dto != null && dto.getCustomerItems() != null && !dto.getCustomerItems().isEmpty();
    }

    private Long splitCustomerOutbound(StockOperateDTO dto) {
        Long firstOrderId = null;
        int totalQuantity = 0;
        for (StockCustomerOutboundItemDTO item : dto.getCustomerItems()) {
            if (item == null) {
                continue;
            }
            requirePositiveQuantity(item.getQuantity());
            if (item.getCustomerId() == null) {
                throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "顧客IDは必須です");
            }
            totalQuantity += item.getQuantity();
        }
        requirePositiveQuantity(totalQuantity);

        for (StockCustomerOutboundItemDTO item : dto.getCustomerItems()) {
            if (item == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                continue;
            }
            StockOperateDTO operation = copyBaseOperation(dto);
            operation.setCustomerId(item.getCustomerId());
            operation.setCustomerName(item.getCustomerName());
            operation.setQuantity(item.getQuantity());
            operation.setRemark(item.getRemark() == null || item.getRemark().isBlank() ? dto.getRemark() : item.getRemark());
            operation.setOutboundMode(co.handk.common.constant.StockBizConstant.OUTBOUND_MODE_CUSTOMER);
            Long orderId = stockService.outbound(operation);
            if (firstOrderId == null) {
                firstOrderId = orderId;
            }
        }
        return firstOrderId == null ? 0L : firstOrderId;
    }

    private StockOperateDTO copyBaseOperation(StockOperateDTO source) {
        StockOperateDTO target = new StockOperateDTO();
        target.setStockId(source.getStockId());
        target.setGoodsId(source.getGoodsId());
        target.setSkuId(source.getSkuId());
        target.setWarehouseId(source.getWarehouseId());
        target.setStockTypeId(source.getStockTypeId());
        target.setSourceType(source.getSourceType());
        target.setDeptId(source.getDeptId());
        target.setGroupCode(source.getGroupCode());
        target.setDeptCode(source.getDeptCode());
        target.setBatchId(source.getBatchId());
        target.setSaleDeadline(source.getSaleDeadline());
        target.setBizDate(source.getBizDate());
        target.setRemark(source.getRemark());
        return target;
    }

    private void requirePositiveQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "数量は1以上で入力してください");
        }
    }

    private Long tryHandleLegacyGroupAllocation(StockOperateDTO dto) {
        List<co.handk.common.model.dto.create.StockGroupAllocationItemDTO> allocations = new ArrayList<>();
        if (dto.getAllocations() != null) {
            for (co.handk.common.model.dto.create.StockGroupAllocationItemDTO item : dto.getAllocations()) {
                if (item == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                    continue;
                }
                allocations.add(item);
            }
        }
        appendAllocation(allocations, "A", dto.getGroupAQty());
        appendAllocation(allocations, "B", dto.getGroupBQty());
        appendAllocation(allocations, "C", dto.getGroupCQty());
        if (allocations.isEmpty()) {
            return null;
        }
        StockGroupAllocateDTO allocateDTO = new StockGroupAllocateDTO();
        allocateDTO.setStockId(dto.getStockId());
        allocateDTO.setGoodsId(dto.getGoodsId());
        allocateDTO.setSkuId(dto.getSkuId());
        allocateDTO.setWarehouseId(dto.getWarehouseId());
        allocateDTO.setStockTypeId(dto.getStockTypeId());
        allocateDTO.setAllocations(allocations);
        allocateDTO.setSaleDeadline(dto.getSaleDeadline());
        allocateDTO.setRemark(dto.getRemark());
        List<Long> orderIds = stockService.allocateToGroups(allocateDTO);
        return orderIds.isEmpty() ? 0L : orderIds.get(0);
    }

    private void appendAllocation(List<co.handk.common.model.dto.create.StockGroupAllocationItemDTO> allocations,
                                  String groupCode,
                                  Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return;
        }
        co.handk.common.model.dto.create.StockGroupAllocationItemDTO item =
                new co.handk.common.model.dto.create.StockGroupAllocationItemDTO();
        item.setGroupCode(groupCode);
        item.setDeptCode(groupCode);
        item.setQuantity(quantity);
        allocations.add(item);
    }

    private boolean hasGroupAllocationTarget(StockOperateDTO dto) {
        if (dto == null) {
            return false;
        }
        if (dto.getDeptId() != null || hasText(dto.getGroupCode()) || hasText(dto.getDeptCode())) {
            return true;
        }
        if (dto.getGroupAQty() != null && dto.getGroupAQty() > 0) {
            return true;
        }
        if (dto.getGroupBQty() != null && dto.getGroupBQty() > 0) {
            return true;
        }
        if (dto.getGroupCQty() != null && dto.getGroupCQty() > 0) {
            return true;
        }
        if (dto.getAllocations() == null) {
            return false;
        }
        return dto.getAllocations().stream()
                .anyMatch(item -> item != null
                        && item.getQuantity() != null
                        && item.getQuantity() > 0
                        && (item.getDeptId() != null
                        || hasText(item.getGroupCode())
                        || hasText(item.getDeptCode())));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
