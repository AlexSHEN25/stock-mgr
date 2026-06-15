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
import co.handk.common.model.dto.create.StockGroupAllocateDTO;
import co.handk.common.model.dto.create.StockGroupAllocationItemDTO;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        inbound.setSourceType(StockBizConstant.INBOUND_SCENE_RESALE);

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
    void customerOutboundIsIdempotentAndConfirmedConsistently() {
        TestFixture fixture = createFixture();
        seedInboundStock(fixture, 10);
        bindRequest("idem-customer-" + UUID.randomUUID());

        StockOperateDTO outbound = baseOperateDTO(fixture, 4);
        outbound.setCustomerId(fixture.customer().getId());
        outbound.setOutboundMode(StockBizConstant.OUTBOUND_MODE_CUSTOMER);

        Long firstOrderId = stockService.outbound(outbound);
        Long secondOrderId = stockService.outbound(outbound);

        assertEquals(firstOrderId, secondOrderId);

        Stock stockBeforeApprove = findStock(fixture);
        List<StockReservation> lockedRows = findReservationsByOrderId(firstOrderId);
        assertEquals(10, stockBeforeApprove.getCurrentQty());
        assertEquals(4, lockedRows.stream().mapToInt(StockReservation::getReservationQty).sum());
        assertTrue(lockedRows.stream().allMatch(row -> row.getState() == StockBizConstant.RESERVATION_STATE_LOCKED));

        assertTrue(stockService.approveOrder(firstOrderId, true, "approve customer outbound"));

        Stock stockAfterApprove = findStock(fixture);
        StockBatch batch = findSingleBatchByStockId(stockAfterApprove.getId());
        List<StockReservation> confirmedRows = findReservationsByOrderId(firstOrderId);

        assertEquals(6, stockAfterApprove.getCurrentQty());
        assertEquals(6, batch.getAvailableQty());
        assertEquals(4, batch.getCustomerOutQty());
        assertTrue(confirmedRows.stream().allMatch(row -> row.getState() == StockBizConstant.RESERVATION_STATE_CONFIRMED));
        assertTrue(confirmedRows.stream().allMatch(row -> row.getConfirmTime() != null));
    }

    @Test
    void rejectedOutboundReleasesReservationWithoutChangingStock() {
        TestFixture fixture = createFixture();
        seedInboundStock(fixture, 8);
        bindRequest("idem-reject-" + UUID.randomUUID());

        StockOperateDTO outbound = baseOperateDTO(fixture, 3);
        outbound.setCustomerId(fixture.customer().getId());
        outbound.setOutboundMode(StockBizConstant.OUTBOUND_MODE_CUSTOMER);

        Long orderId = stockService.outbound(outbound);
        assertTrue(stockService.approveOrder(orderId, false, "reject outbound"));

        Stock stock = findStock(fixture);
        StockBatch batch = findSingleBatchByStockId(stock.getId());
        List<StockReservation> releasedRows = findReservationsByOrderId(orderId);

        assertEquals(8, stock.getCurrentQty());
        assertEquals(8, batch.getAvailableQty());
        assertEquals(0, batch.getAllocatedQty());
        assertEquals(0, batch.getCustomerOutQty());
        assertTrue(releasedRows.stream().allMatch(row -> row.getState() == StockBizConstant.RESERVATION_STATE_RELEASED));
        assertTrue(releasedRows.stream().allMatch(row -> row.getReleaseTime() != null));
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
        assertTrue(stockService.approveOrder(allocationOrders.get(0), true, "approve allocation"));

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
        assertTrue(stockService.approveOrder(orderId, true, "approve group customer outbound"));

        Stock stockAfterGroupOutbound = findStock(fixture);
        GroupStock groupStockAfterOutbound = findSingleGroupStock(stockAfterGroupOutbound.getId(), fixture.groupDept().getId());
        List<StockReservation> confirmedRows = findReservationsByOrderId(orderId);

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
        assertTrue(stockService.approveOrder(allocationOrders.get(0), true, "approve expired allocation"));

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

    private void seedInboundStock(TestFixture fixture, int quantity) {
        StockOperateDTO inbound = baseOperateDTO(fixture, quantity);
        inbound.setSourceType(StockBizConstant.INBOUND_SCENE_RESALE);
        Long orderId = stockService.inbound(inbound);
        assertNotNull(orderId);
    }

    private StockOperateDTO baseOperateDTO(TestFixture fixture, int quantity) {
        StockOperateDTO dto = new StockOperateDTO();
        dto.setGoodsId(fixture.goods().getId().intValue());
        dto.setSkuId(fixture.sku().getId());
        dto.setWarehouseId(fixture.warehouse().getId().intValue());
        dto.setStockTypeId(fixture.stockType().getId());
        dto.setQuantity(quantity);
        return dto;
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
