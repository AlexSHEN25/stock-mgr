package co.handk.backend.integration;

import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.entity.Brand;
import co.handk.backend.entity.Category;
import co.handk.backend.entity.Customer;
import co.handk.backend.entity.Dept;
import co.handk.backend.entity.Goods;
import co.handk.backend.entity.GoodsSku;
import co.handk.backend.entity.GroupStock;
import co.handk.backend.entity.Stock;
import co.handk.backend.entity.StockBatch;
import co.handk.backend.entity.StockOrder;
import co.handk.backend.entity.StockOrderItem;
import co.handk.backend.entity.StockReservation;
import co.handk.backend.entity.StockType;
import co.handk.backend.entity.Warehouse;
import co.handk.backend.mapper.GroupStockMapper;
import co.handk.backend.mapper.StockBatchMapper;
import co.handk.backend.mapper.StockReservationMapper;
import co.handk.backend.service.BrandService;
import co.handk.backend.service.CategoryService;
import co.handk.backend.service.CustomerService;
import co.handk.backend.service.DeptService;
import co.handk.backend.service.GoodsService;
import co.handk.backend.service.GoodsSkuService;
import co.handk.backend.service.PermissionQueryService;
import co.handk.backend.service.StockOrderItemService;
import co.handk.backend.service.StockOrderService;
import co.handk.backend.service.StockBatchService;
import co.handk.backend.service.StockService;
import co.handk.backend.service.StockTypeService;
import co.handk.backend.service.WarehouseService;
import co.handk.backend.util.StringRedisUtil;
import co.handk.common.constant.StockBizConstant;
import co.handk.common.model.dto.create.StockBatchOperateDTO;
import co.handk.common.model.dto.create.StockBatchOperateItemDTO;
import co.handk.common.model.dto.create.StockGroupAllocateDTO;
import co.handk.common.model.dto.create.StockGroupAllocationItemDTO;
import co.handk.common.model.dto.create.StockOrderSubmitDTO;
import co.handk.common.model.dto.create.StockOrderSubmitItemDTO;
import co.handk.common.model.dto.create.StockOperateDTO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
@Transactional
class StockFlowIntegrationTest {

    private static final Long ADMIN_USER_ID = 1L;

    @Autowired
    private StockService stockService;
    @Autowired
    private StockOrderService stockOrderService;
    @Autowired
    private StockOrderItemService stockOrderItemService;
    @Autowired
    private StockBatchService stockBatchService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private GoodsSkuService goodsSkuService;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private StockTypeService stockTypeService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private DeptService deptService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private StockBatchMapper stockBatchMapper;
    @Autowired
    private GroupStockMapper groupStockMapper;
    @Autowired
    private StockReservationMapper stockReservationMapper;
    @Autowired
    private PermissionQueryService permissionQueryService;

    @MockBean
    private StringRedisUtil stringRedisUtil;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(ADMIN_USER_ID);
        bindRequest(null);
        when(stringRedisUtil.setIfAbsent(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.any(TimeUnit.class)))
                .thenReturn(true);
        when(stringRedisUtil.get(ArgumentMatchers.anyString())).thenReturn(null);
        when(stringRedisUtil.delete(ArgumentMatchers.anyString())).thenReturn(true);
        assertTrue(permissionQueryService.isSuperAdmin(ADMIN_USER_ID), "admin user must be super admin for stock flow tests");
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        UserContext.clear();
    }

    @Test
    void manualInboundCreatesFinishedOrderAndBatch() {
        TestFixture fixture = createFixture();
        StockOperateDTO inbound = baseOperateDTO(fixture, 10);
        inbound.setSourceType(StockBizConstant.INBOUND_SCENE_SELF);

        Long orderId = stockService.inbound(inbound);

        StockOrder order = stockOrderService.getByIdNotDeleted(orderId);
        Stock stock = findStock(fixture);
        StockBatch batch = findSingleBatchByStockId(stock.getId());

        assertEquals(StockBizConstant.ORDER_STATE_FINISHED, order.getState());
        assertEquals(10, stock.getCurrentQty());
        assertEquals(10, batch.getAvailableQty());
        assertEquals(0, batch.getAllocatedQty());
        assertEquals(0, batch.getCustomerOutQty());
    }

    @Test
    void adminCustomerOutboundIsIdempotentAndAutoConfirmedConsistently() {
        TestFixture fixture = createFixture();
        seedInboundStock(fixture, 10);
        bindRequest("idem-customer-" + UUID.randomUUID());

        StockOperateDTO outbound = baseOperateDTO(fixture, 4);
        outbound.setCustomerId(fixture.customer().getId());
        outbound.setOutboundMode(StockBizConstant.OUTBOUND_MODE_CUSTOMER);

        Long firstOrderId = stockService.outbound(outbound);
        Long secondOrderId = stockService.outbound(outbound);

        assertEquals(firstOrderId, secondOrderId);

        Stock stockAfterApprove = findStock(fixture);
        StockOrder order = stockOrderService.getByIdNotDeleted(firstOrderId);
        StockBatch batch = findSingleBatchByStockId(stockAfterApprove.getId());
        List<StockReservation> confirmedRows = findReservationsByOrderId(firstOrderId);

        assertEquals(StockBizConstant.ORDER_STATE_FINISHED, order.getState());
        assertEquals(6, stockAfterApprove.getCurrentQty());
        assertEquals(6, batch.getAvailableQty());
        assertEquals(4, batch.getCustomerOutQty());
        assertTrue(confirmedRows.stream().allMatch(row -> row.getState() == StockBizConstant.RESERVATION_STATE_CONFIRMED));
        assertTrue(confirmedRows.stream().allMatch(row -> row.getConfirmTime() != null));
    }

    @Test
    void adminCustomerOutboundAutoApprovalDecrementsStockImmediately() {
        TestFixture fixture = createFixture();
        seedInboundStock(fixture, 8);
        bindRequest("idem-auto-approve-" + UUID.randomUUID());

        StockOperateDTO outbound = baseOperateDTO(fixture, 3);
        outbound.setCustomerId(fixture.customer().getId());
        outbound.setOutboundMode(StockBizConstant.OUTBOUND_MODE_CUSTOMER);

        Long orderId = stockService.outbound(outbound);

        Stock stock = findStock(fixture);
        StockOrder order = stockOrderService.getByIdNotDeleted(orderId);
        StockBatch batch = findSingleBatchByStockId(stock.getId());
        List<StockReservation> confirmedRows = findReservationsByOrderId(orderId);

        assertEquals(StockBizConstant.ORDER_STATE_FINISHED, order.getState());
        assertEquals(5, stock.getCurrentQty());
        assertEquals(5, batch.getAvailableQty());
        assertEquals(0, batch.getAllocatedQty());
        assertEquals(3, batch.getCustomerOutQty());
        assertTrue(confirmedRows.stream().allMatch(row -> row.getState() == StockBizConstant.RESERVATION_STATE_CONFIRMED));
        assertTrue(confirmedRows.stream().allMatch(row -> row.getConfirmTime() != null));
    }

    @Test
    void outboundApprovalRejectRestoresBlockedStock() {
        TestFixture fixture = createFixture();
        seedInboundStock(fixture, 9);
        Stock stock = findStock(fixture);

        StockOrderSubmitItemDTO item = new StockOrderSubmitItemDTO();
        item.setStockId(stock.getId());
        item.setQuantity(4);

        StockOrderSubmitDTO submit = new StockOrderSubmitDTO();
        submit.setOrderType(StockBizConstant.ORDER_TYPE_OUTBOUND);
        submit.setItems(List.of(item));

        Long orderId = stockService.submitOrder(submit);

        Stock blockedStock = findStock(fixture);
        StockBatch blockedBatch = findSingleBatchByStockId(blockedStock.getId());
        List<StockReservation> lockedRows = findReservationsByOrderId(orderId);
        StockOrder order = stockOrderService.getByIdNotDeleted(orderId);

        assertEquals(StockBizConstant.ORDER_STATE_APPROVING, order.getState());
        assertEquals(5, blockedStock.getCurrentQty());
        assertEquals(5, blockedBatch.getAvailableQty());
        assertEquals(1, lockedRows.size());
        assertEquals(StockBizConstant.RESERVATION_STATE_LOCKED, lockedRows.get(0).getState());

        assertTrue(stockService.approveOrder(orderId, false, "reject"));
        assertTrue(stockService.approveOrder(orderId, false, "reject again"));

        Stock restoredStock = findStock(fixture);
        StockBatch restoredBatch = findSingleBatchByStockId(restoredStock.getId());
        List<StockReservation> releasedRows = findReservationsByOrderId(orderId);
        StockOrder rejectedOrder = stockOrderService.getByIdNotDeleted(orderId);

        assertEquals(StockBizConstant.ORDER_STATE_CANCELED, rejectedOrder.getState());
        assertEquals(9, restoredStock.getCurrentQty());
        assertEquals(9, restoredBatch.getAvailableQty());
        assertEquals(StockBizConstant.RESERVATION_STATE_RELEASED, releasedRows.get(0).getState());
        assertNotNull(releasedRows.get(0).getReleaseTime());
    }

    @Test
    void groupAllocateThenGroupCustomerOutboundStaysConsistent() {
        TestFixture fixture = createFixture();
        seedInboundStock(fixture, 12);

        Stock stock = findStock(fixture);
        StockGroupAllocationItemDTO allocationItem = new StockGroupAllocationItemDTO();
        allocationItem.setDeptId(fixture.groupDept().getId());
        allocationItem.setDeptCode(fixture.groupDept().getCode());
        allocationItem.setGroupCode(fixture.groupDept().getCode());
        allocationItem.setQuantity(5);

        StockGroupAllocateDTO allocateDTO = new StockGroupAllocateDTO();
        allocateDTO.setStockId(stock.getId());
        allocateDTO.setGoodsId(fixture.goods().getId().intValue());
        allocateDTO.setSkuId(fixture.sku().getId());
        allocateDTO.setWarehouseId(fixture.warehouse().getId().intValue());
        allocateDTO.setStockTypeId(fixture.stockType().getId());
        allocateDTO.setSaleDeadline(LocalDateTime.now().plusDays(30));
        allocateDTO.setAllocations(List.of(allocationItem));

        List<Long> allocationOrders = stockService.allocateToGroups(allocateDTO);
        assertEquals(1, allocationOrders.size());

        Stock stockAfterAllocation = findStock(fixture);
        GroupStock groupStockAfterAllocation = findSingleGroupStock(stockAfterAllocation.getId(), fixture.groupDept().getId());
        assertEquals(7, stockAfterAllocation.getCurrentQty());
        assertEquals(5, groupStockAfterAllocation.getCurrentQty());

        bindRequest("idem-group-customer-" + UUID.randomUUID());
        StockOperateDTO groupCustomerOutbound = baseOperateDTO(fixture, 2);
        groupCustomerOutbound.setCustomerId(fixture.customer().getId());
        groupCustomerOutbound.setDeptId(fixture.groupDept().getId());
        groupCustomerOutbound.setGroupCode(fixture.groupDept().getCode());
        groupCustomerOutbound.setOutboundMode(StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER);

        Long orderId = stockService.outbound(groupCustomerOutbound);

        Stock stockAfterGroupOutbound = findStock(fixture);
        StockOrder order = stockOrderService.getByIdNotDeleted(orderId);
        GroupStock groupStockAfterOutbound = findSingleGroupStock(stockAfterGroupOutbound.getId(), fixture.groupDept().getId());
        List<StockReservation> confirmedRows = findReservationsByOrderId(orderId);

        assertEquals(StockBizConstant.ORDER_STATE_FINISHED, order.getState());
        assertEquals(7, stockAfterGroupOutbound.getCurrentQty());
        assertEquals(3, groupStockAfterOutbound.getCurrentQty());
        assertTrue(confirmedRows.stream().allMatch(row -> row.getState() == StockBizConstant.RESERVATION_STATE_CONFIRMED));
        assertEquals(StockBizConstant.RESERVATION_SCOPE_GROUP, confirmedRows.get(0).getReservationScope());
    }

    @Test
    void reclaimExpiredGroupStockReturnsQuantityToSelfBatchAndStock() {
        TestFixture fixture = createFixture();
        seedInboundStock(fixture, 6);

        Stock stock = findStock(fixture);
        StockGroupAllocationItemDTO allocationItem = new StockGroupAllocationItemDTO();
        allocationItem.setDeptId(fixture.groupDept().getId());
        allocationItem.setDeptCode(fixture.groupDept().getCode());
        allocationItem.setGroupCode(fixture.groupDept().getCode());
        allocationItem.setQuantity(4);

        StockGroupAllocateDTO allocateDTO = new StockGroupAllocateDTO();
        allocateDTO.setStockId(stock.getId());
        allocateDTO.setGoodsId(fixture.goods().getId().intValue());
        allocateDTO.setSkuId(fixture.sku().getId());
        allocateDTO.setWarehouseId(fixture.warehouse().getId().intValue());
        allocateDTO.setStockTypeId(fixture.stockType().getId());
        allocateDTO.setSaleDeadline(LocalDateTime.now().minusDays(1));
        allocateDTO.setAllocations(List.of(allocationItem));

        List<Long> allocationOrders = stockService.allocateToGroups(allocateDTO);
        assertEquals(1, allocationOrders.size());

        int reclaimedQty = stockBatchService.reclaimExpiredGroupStock();

        Stock stockAfterReclaim = findStock(fixture);
        StockBatch batchAfterReclaim = findSingleBatchByStockId(stockAfterReclaim.getId());
        GroupStock groupAfterReclaim = findSingleGroupStock(stockAfterReclaim.getId(), fixture.groupDept().getId());

        assertEquals(4, reclaimedQty);
        assertEquals(6, stockAfterReclaim.getCurrentQty());
        assertEquals(6, batchAfterReclaim.getCurrentQty());
        assertEquals(6, batchAfterReclaim.getAvailableQty());
        assertEquals(0, batchAfterReclaim.getAllocatedQty());
        assertEquals(0, groupAfterReclaim.getCurrentQty());
        assertEquals(StockBizConstant.BATCH_STATE_EXPIRED, groupAfterReclaim.getState());
    }

    @Test
    void selfCustomerOutboundOverAvailableQuantityDoesNotCreateOrderOrChangeStock() {
        TestFixture fixture = createFixture();
        seedInboundStock(fixture, 5);
        Stock stockBefore = findStock(fixture);
        StockBatch batchBefore = findSingleBatchByStockId(stockBefore.getId());
        long orderCountBefore = countStockOrders(fixture);

        bindRequest("idem-self-over-" + UUID.randomUUID());
        StockOperateDTO outbound = baseOperateDTO(fixture, 6);
        outbound.setCustomerId(fixture.customer().getId());
        outbound.setOutboundMode(StockBizConstant.OUTBOUND_MODE_CUSTOMER);

        assertThrows(RuntimeException.class, () -> stockService.outbound(outbound));

        Stock stockAfter = findStock(fixture);
        StockBatch batchAfter = findSingleBatchByStockId(stockAfter.getId());
        assertEquals(orderCountBefore, countStockOrders(fixture));
        assertEquals(5, stockAfter.getCurrentQty());
        assertEquals(batchBefore.getCurrentQty(), batchAfter.getCurrentQty());
        assertEquals(5, batchAfter.getAvailableQty());
        assertEquals(0, batchAfter.getCustomerOutQty());
    }

    @Test
    void groupCustomerOutboundOverGroupQuantityDoesNotChangeSelfOrGroupStock() {
        TestFixture fixture = createFixture();
        seedInboundStock(fixture, 10);
        allocateToSingleGroup(fixture, 4, LocalDateTime.now().plusDays(30));

        Stock stockBefore = findStock(fixture);
        StockBatch batchBefore = findSingleBatchByStockId(stockBefore.getId());
        GroupStock groupBefore = findSingleGroupStock(stockBefore.getId(), fixture.groupDept().getId());
        long orderCountBefore = countStockOrders(fixture);

        bindRequest("idem-group-over-" + UUID.randomUUID());
        StockOperateDTO groupOutbound = baseOperateDTO(fixture, 5);
        groupOutbound.setCustomerId(fixture.customer().getId());
        groupOutbound.setDeptId(fixture.groupDept().getId());
        groupOutbound.setGroupCode(fixture.groupDept().getCode());
        groupOutbound.setOutboundMode(StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER);

        assertThrows(RuntimeException.class, () -> stockService.outbound(groupOutbound));

        Stock stockAfter = findStock(fixture);
        StockBatch batchAfter = findSingleBatchByStockId(stockAfter.getId());
        GroupStock groupAfter = findSingleGroupStock(stockAfter.getId(), fixture.groupDept().getId());
        assertEquals(orderCountBefore, countStockOrders(fixture));
        assertEquals(stockBefore.getCurrentQty(), stockAfter.getCurrentQty());
        assertEquals(batchBefore.getCurrentQty(), batchAfter.getCurrentQty());
        assertEquals(batchBefore.getAvailableQty(), batchAfter.getAvailableQty());
        assertEquals(groupBefore.getCurrentQty(), groupAfter.getCurrentQty());
    }

    @Test
    void groupAllocationOverSelfQuantityDoesNotCreatePartialGroupStock() {
        TestFixture fixture = createFixture();
        seedInboundStock(fixture, 3);
        Stock stockBefore = findStock(fixture);
        StockBatch batchBefore = findSingleBatchByStockId(stockBefore.getId());
        long orderCountBefore = countStockOrders(fixture);

        StockGroupAllocationItemDTO allocationItem = new StockGroupAllocationItemDTO();
        allocationItem.setDeptId(fixture.groupDept().getId());
        allocationItem.setDeptCode(fixture.groupDept().getCode());
        allocationItem.setGroupCode(fixture.groupDept().getCode());
        allocationItem.setQuantity(4);

        StockGroupAllocateDTO allocateDTO = new StockGroupAllocateDTO();
        allocateDTO.setStockId(stockBefore.getId());
        allocateDTO.setGoodsId(fixture.goods().getId().intValue());
        allocateDTO.setSkuId(fixture.sku().getId());
        allocateDTO.setWarehouseId(fixture.warehouse().getId().intValue());
        allocateDTO.setStockTypeId(fixture.stockType().getId());
        allocateDTO.setSaleDeadline(LocalDateTime.now().plusDays(30));
        allocateDTO.setAllocations(List.of(allocationItem));

        assertThrows(RuntimeException.class, () -> stockService.allocateToGroups(allocateDTO));

        Stock stockAfter = findStock(fixture);
        StockBatch batchAfter = findSingleBatchByStockId(stockAfter.getId());
        assertEquals(orderCountBefore, countStockOrders(fixture));
        assertEquals(3, stockAfter.getCurrentQty());
        assertEquals(batchBefore.getCurrentQty(), batchAfter.getCurrentQty());
        assertEquals(3, batchAfter.getAvailableQty());
        assertEquals(0, countGroupStockRows(stockAfter.getId(), fixture.groupDept().getId()));
    }

    @Test
    void batchInboundMultipleItemsUsesItemScopedIdempotencyKeys() {
        TestFixture fixture = createFixture();
        StockBatchOperateDTO batch = new StockBatchOperateDTO();
        batch.setSourceType(StockBizConstant.INBOUND_SCENE_SELF);
        batch.setRemark("batch-idempotency");

        StockBatchOperateItemDTO firstItem = baseBatchInboundItem(fixture, 2);
        StockBatchOperateItemDTO secondItem = baseBatchInboundItem(fixture, 3);
        batch.setItems(List.of(firstItem, secondItem));

        Long firstOrderId = stockService.batchInbound(batch);
        assertNotNull(firstOrderId);
        assertEquals(2, countStockOrders(fixture));
        assertEquals(5, findStock(fixture).getCurrentQty());

        Long repeatedOrderId = stockService.batchInbound(batch);
        assertEquals(firstOrderId, repeatedOrderId);
        assertEquals(2, countStockOrders(fixture));
        assertEquals(5, findStock(fixture).getCurrentQty());
    }

    @Test
    void batchInboundHeaderIdempotencyKeyEscapesSqlLikeWildcards() {
        TestFixture fixture = createFixture();
        StockBatchOperateDTO batch = new StockBatchOperateDTO();
        batch.setSourceType(StockBizConstant.INBOUND_SCENE_SELF);
        batch.setItems(List.of(baseBatchInboundItem(fixture, 2)));

        bindRequest("idem%wild_key-" + UUID.randomUUID());
        Long firstOrderId = stockService.batchInbound(batch);
        assertNotNull(firstOrderId);

        Long repeatedOrderId = stockService.batchInbound(batch);
        assertEquals(firstOrderId, repeatedOrderId);
        assertEquals(1, countStockOrders(fixture));
        assertEquals(2, findStock(fixture).getCurrentQty());
    }

    private void seedInboundStock(TestFixture fixture, int quantity) {
        StockOperateDTO inbound = baseOperateDTO(fixture, quantity);
        inbound.setSourceType(StockBizConstant.INBOUND_SCENE_SELF);
        Long orderId = stockService.inbound(inbound);
        assertNotNull(orderId);
    }

    private List<Long> allocateToSingleGroup(TestFixture fixture, int quantity, LocalDateTime saleDeadline) {
        Stock stock = findStock(fixture);
        StockGroupAllocationItemDTO allocationItem = new StockGroupAllocationItemDTO();
        allocationItem.setDeptId(fixture.groupDept().getId());
        allocationItem.setDeptCode(fixture.groupDept().getCode());
        allocationItem.setGroupCode(fixture.groupDept().getCode());
        allocationItem.setQuantity(quantity);

        StockGroupAllocateDTO allocateDTO = new StockGroupAllocateDTO();
        allocateDTO.setStockId(stock.getId());
        allocateDTO.setGoodsId(fixture.goods().getId().intValue());
        allocateDTO.setSkuId(fixture.sku().getId());
        allocateDTO.setWarehouseId(fixture.warehouse().getId().intValue());
        allocateDTO.setStockTypeId(fixture.stockType().getId());
        allocateDTO.setSaleDeadline(saleDeadline);
        allocateDTO.setAllocations(List.of(allocationItem));
        return stockService.allocateToGroups(allocateDTO);
    }

    private StockOperateDTO baseOperateDTO(TestFixture fixture, int quantity) {
        StockOperateDTO dto = new StockOperateDTO();
        dto.setGoodsId(fixture.goods().getId().intValue());
        dto.setSkuId(fixture.sku().getId());
        dto.setWarehouseId(fixture.warehouse().getId().intValue());
        dto.setStockTypeId(fixture.stockType().getId());
        dto.setQuantity(quantity);
        dto.setBizDate(LocalDate.now());
        return dto;
    }

    private StockBatchOperateItemDTO baseBatchInboundItem(TestFixture fixture, int quantity) {
        StockBatchOperateItemDTO item = new StockBatchOperateItemDTO();
        item.setGoodsId(fixture.goods().getId().intValue());
        item.setSkuId(fixture.sku().getId());
        item.setWarehouseId(fixture.warehouse().getId().intValue());
        item.setStockTypeId(fixture.stockType().getId());
        item.setQuantity(quantity);
        item.setBizDate(LocalDate.now());
        return item;
    }

    private TestFixture createFixture() {
        Brand brand = new Brand();
        brand.setName("brand-" + UUID.randomUUID());
        assertTrue(brandService.save(brand));

        Category category = new Category();
        category.setName("category-" + UUID.randomUUID());
        assertTrue(categoryService.save(category));

        Goods goods = new Goods();
        goods.setName("goods-" + UUID.randomUUID());
        goods.setBrandId(brand.getId());
        goods.setCategoryId(category.getId());
        goods.setIsHot(0);
        goods.setSort(1);
        assertTrue(goodsService.save(goods));

        GoodsSku sku = new GoodsSku();
        sku.setGoodsId(goods.getId());
        sku.setSkuCode("SKU-" + UUID.randomUUID());
        sku.setSkuName("sku-" + UUID.randomUUID());
        sku.setPrice(new BigDecimal("1000"));
        sku.setCurrency("JPY");
        sku.setStatus(1);
        assertTrue(goodsSkuService.save(sku));

        Warehouse warehouse = new Warehouse();
        warehouse.setName("warehouse-" + UUID.randomUUID());
        warehouse.setCode("WH-" + UUID.randomUUID());
        warehouse.setAddress("test");
        warehouse.setStatus(1);
        assertTrue(warehouseService.save(warehouse));

        StockType stockType = new StockType();
        stockType.setName("stock-type-" + UUID.randomUUID());
        stockType.setStatus(1);
        assertTrue(stockTypeService.save(stockType));

        Dept groupDept = resolveOrCreateGroupDept("A");

        Customer customer = new Customer();
        customer.setCustomerCode("CUS-" + UUID.randomUUID());
        customer.setName("customer-" + UUID.randomUUID());
        customer.setCountry("JP");
        customer.setOwnerUserId(ADMIN_USER_ID);
        customer.setOwnerDeptId(groupDept.getId());
        customer.setStatus(1);
        assertTrue(customerService.save(customer));

        return new TestFixture(goods, sku, warehouse, stockType, customer, groupDept);
    }

    private Dept resolveOrCreateGroupDept(String code) {
        Dept existing = deptService.getOne(new QueryWrapper<Dept>()
                .eq("code", code)
                .eq("deleted", 0)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }
        Dept dept = new Dept();
        dept.setName(code + "-test");
        dept.setCode(code);
        dept.setStatus(1);
        dept.setSort(1);
        assertTrue(deptService.save(dept));
        return dept;
    }

    private Stock findStock(TestFixture fixture) {
        Stock stock = stockService.getOne(new QueryWrapper<Stock>()
                .eq("goods_id", fixture.goods().getId())
                .eq("sku_id", fixture.sku().getId())
                .eq("warehouse_id", fixture.warehouse().getId())
                .eq("stock_type_id", fixture.stockType().getId())
                .eq("deleted", 0)
                .last("LIMIT 1"));
        assertNotNull(stock);
        return stock;
    }

    private StockBatch findSingleBatchByStockId(Long stockId) {
        StockBatch batch = stockBatchMapper.selectOne(new QueryWrapper<StockBatch>()
                .eq("stock_id", stockId)
                .eq("deleted", 0)
                .last("LIMIT 1"));
        assertNotNull(batch);
        return batch;
    }

    private GroupStock findSingleGroupStock(Long stockId, Long deptId) {
        GroupStock groupStock = groupStockMapper.selectOne(new QueryWrapper<GroupStock>()
                .eq("stock_id", stockId)
                .eq("dept_id", deptId)
                .eq("deleted", 0)
                .last("LIMIT 1"));
        assertNotNull(groupStock);
        return groupStock;
    }

    private List<StockReservation> findReservationsByOrderId(Long orderId) {
        return stockReservationMapper.selectList(new QueryWrapper<StockReservation>()
                .eq("order_id", orderId)
                .eq("deleted", 0)
                .orderByAsc("id"));
    }

    private long countStockOrders(TestFixture fixture) {
        return stockOrderService.count(new QueryWrapper<StockOrder>()
                .eq("warehouse_id", fixture.warehouse().getId())
                .eq("stock_type_id", fixture.stockType().getId())
                .eq("deleted", 0));
    }

    private long countGroupStockRows(Long stockId, Long deptId) {
        return groupStockMapper.selectCount(new QueryWrapper<GroupStock>()
                .eq("stock_id", stockId)
                .eq("dept_id", deptId)
                .eq("deleted", 0));
    }

    private void bindRequest(String idempotencyKey) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (idempotencyKey != null) {
            request.addHeader("Idempotency-Key", idempotencyKey);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private record TestFixture(
            Goods goods,
            GoodsSku sku,
            Warehouse warehouse,
            StockType stockType,
            Customer customer,
            Dept groupDept
    ) {
    }
}
