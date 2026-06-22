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
import co.handk.backend.mapper.GroupStockMapper;
import co.handk.backend.mapper.StockBatchMapper;
import co.handk.backend.service.BrandService;
import co.handk.backend.service.CategoryService;
import co.handk.backend.service.CustomerService;
import co.handk.backend.service.DeptService;
import co.handk.backend.service.GoodsService;
import co.handk.backend.service.GoodsSkuService;
import co.handk.backend.service.PermissionQueryService;
import co.handk.backend.service.StockOrderService;
import co.handk.backend.service.StockService;
import co.handk.backend.service.StockTypeService;
import co.handk.backend.service.WarehouseService;
import co.handk.backend.util.StringRedisUtil;
import co.handk.common.constant.RedisKey;
import co.handk.common.constant.StockBizConstant;
import co.handk.common.model.dto.create.StockGroupAllocateDTO;
import co.handk.common.model.dto.create.StockGroupAllocationItemDTO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class StockControllerIntegrationTest {

    private static final String API_CONTEXT_PATH = "/api";
    private static final Long ADMIN_USER_ID = 1L;
    private static final String ADMIN_TOKEN = "test-admin-token";
    private static final Long SALES01_USER_ID = 2L;
    private static final String SALES01_TOKEN = "test-sales01-token";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
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
    private StockService stockService;
    @Autowired
    private StockOrderService stockOrderService;
    @Autowired
    private StockBatchMapper stockBatchMapper;
    @Autowired
    private GroupStockMapper groupStockMapper;
    @Autowired
    private PermissionQueryService permissionQueryService;

    @MockBean
    private StringRedisUtil stringRedisUtil;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(ADMIN_USER_ID);
        when(stringRedisUtil.setIfAbsent(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.any(TimeUnit.class)))
                .thenReturn(true);
        when(stringRedisUtil.getExpire(ArgumentMatchers.anyString(), ArgumentMatchers.any(TimeUnit.class)))
                .thenReturn(null);
        when(stringRedisUtil.expire(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
                .thenReturn(true);
        when(stringRedisUtil.delete(ArgumentMatchers.anyString())).thenReturn(true);
        when(stringRedisUtil.get(ArgumentMatchers.anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            if ((RedisKey.LOGIN_TOKEN + ADMIN_TOKEN).equals(key)) {
                return String.valueOf(ADMIN_USER_ID);
            }
            if ((RedisKey.LOGIN_USER + ADMIN_USER_ID).equals(key)) {
                return ADMIN_TOKEN;
            }
            if ((RedisKey.LOGIN_TOKEN + SALES01_TOKEN).equals(key)) {
                return String.valueOf(SALES01_USER_ID);
            }
            if ((RedisKey.LOGIN_USER + SALES01_USER_ID).equals(key)) {
                return SALES01_TOKEN;
            }
            return null;
        });
        assertTrue(permissionQueryService.isSuperAdmin(ADMIN_USER_ID));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void inboundEndpointCreatesStockBatchSuccessfully() throws Exception {
        TestFixture fixture = createFixture();
        String payload = """
                {
                  "goodsId": %d,
                  "skuId": %d,
                  "warehouseId": %d,
                  "stockTypeId": %d,
                  "quantity": 9,
                  "sourceType": 1
                }
                """.formatted(
                fixture.goods().getId(),
                fixture.sku().getId(),
                fixture.warehouse().getId(),
                fixture.stockType().getId());

        String response = mockMvc.perform(apiPost("/stock/inbound")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long orderId = objectMapper.readTree(response).path("data").asLong();
        UserContext.setUserId(ADMIN_USER_ID);
        StockOrder order = stockOrderService.getByIdNotDeleted(orderId);
        Stock stock = findStock(fixture);
        StockBatch batch = findSingleBatchByStockId(stock.getId());

        assertNotNull(order);
        assertEquals(StockBizConstant.ORDER_STATE_FINISHED, order.getState());
        assertEquals(9, stock.getCurrentQty());
        assertEquals(9, batch.getCurrentQty());
        assertEquals(9, batch.getAvailableQty());
    }

    @Test
    void inboundEndpointRejectsPurchaseInbound() throws Exception {
        TestFixture fixture = createFixture();
        String payload = """
                {
                  "goodsId": %d,
                  "skuId": %d,
                  "warehouseId": %d,
                  "stockTypeId": %d,
                  "quantity": 9,
                  "sourceType": 2
                }
                """.formatted(
                fixture.goods().getId(),
                fixture.sku().getId(),
                fixture.warehouse().getId(),
                fixture.stockType().getId());

        mockMvc.perform(apiPost("/stock/inbound")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("purchase inbound has been disabled"));
    }

    @Test
    void customerOutboundEndpointIsIdempotentAcrossRepeatedPosts() throws Exception {
        TestFixture fixture = createFixture();
        seedInboundStock(fixture, 10);
        String idemKey = "http-idem-" + UUID.randomUUID();
        String payload = """
                {
                  "goodsId": %d,
                  "skuId": %d,
                  "warehouseId": %d,
                  "stockTypeId": %d,
                  "quantity": 4,
                  "customerId": %d
                }
                """.formatted(
                fixture.goods().getId(),
                fixture.sku().getId(),
                fixture.warehouse().getId(),
                fixture.stockType().getId(),
                fixture.customer().getId());

        String first = mockMvc.perform(apiPost("/stock/customer/outbound")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .header("Idempotency-Key", idemKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String second = mockMvc.perform(apiPost("/stock/customer/outbound")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .header("Idempotency-Key", idemKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long firstOrderId = objectMapper.readTree(first).path("data").asLong();
        Long secondOrderId = objectMapper.readTree(second).path("data").asLong();
        assertEquals(firstOrderId, secondOrderId);
    }

    @Test
    void approveEndpointConfirmsGroupCustomerOutboundThroughHttp() throws Exception {
        TestFixture fixture = createFixture();
        seedInboundStock(fixture, 8);
        Long allocationOrderId = createAndApproveAllocation(fixture, 5, LocalDateTime.now().plusDays(30));

        String outboundPayload = """
                {
                  "goodsId": %d,
                  "skuId": %d,
                  "warehouseId": %d,
                  "stockTypeId": %d,
                  "quantity": 2,
                  "customerId": %d,
                  "groupCode": "%s"
                }
                """.formatted(
                fixture.goods().getId(),
                fixture.sku().getId(),
                fixture.warehouse().getId(),
                fixture.stockType().getId(),
                fixture.customer().getId(),
                fixture.groupDept().getCode());

        String outboundResponse = mockMvc.perform(apiPost("/stock/group/customer/outbound")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .header("Idempotency-Key", "group-http-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(outboundPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long outboundOrderId = objectMapper.readTree(outboundResponse).path("data").asLong();

        mockMvc.perform(apiPost("/stock/approve/" + outboundOrderId)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .param("approved", "true")
                        .param("remark", "approve by http"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        Stock stock = findStock(fixture);
        GroupStock groupStock = findSingleGroupStock(stock.getId(), fixture.groupDept().getId());
        StockBatch batch = findSingleBatchByStockId(stock.getId());

        assertEquals(3, stock.getCurrentQty());
        assertEquals(3, batch.getAvailableQty());
        assertEquals(3, groupStock.getCurrentQty());
    }

    @Test
    void normalUserCannotAllocateGroupStockOrApproveOrders() throws Exception {
        TestFixture fixture = createFixture();
        seedInboundStock(fixture, 6);

        String allocatePayload = """
                {
                  "stockId": %d,
                  "goodsId": %d,
                  "skuId": %d,
                  "warehouseId": %d,
                  "stockTypeId": %d,
                  "allocations": [
                    {
                      "groupCode": "A",
                      "deptCode": "A",
                      "quantity": 2
                    }
                  ]
                }
                """.formatted(
                findStock(fixture).getId(),
                fixture.goods().getId(),
                fixture.sku().getId(),
                fixture.warehouse().getId(),
                fixture.stockType().getId());

        mockMvc.perform(apiPost("/stock/group/allocate")
                        .header("Authorization", "Bearer " + SALES01_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(allocatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("権限がありません"));

        String outboundPayload = """
                {
                  "goodsId": %d,
                  "skuId": %d,
                  "warehouseId": %d,
                  "stockTypeId": %d,
                  "quantity": 1,
                  "customerId": %d
                }
                """.formatted(
                fixture.goods().getId(),
                fixture.sku().getId(),
                fixture.warehouse().getId(),
                fixture.stockType().getId(),
                fixture.customer().getId());

        String outboundResponse = mockMvc.perform(apiPost("/stock/customer/outbound")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .header("Idempotency-Key", "normal-approve-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(outboundPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long outboundOrderId = objectMapper.readTree(outboundResponse).path("data").asLong();

        mockMvc.perform(apiPost("/stock/approve/" + outboundOrderId)
                        .header("Authorization", "Bearer " + SALES01_TOKEN)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("権限がありません"));
    }

    @Test
    void normalUserCanOutboundOwnGroupButCannotUseOtherGroup() throws Exception {
        TestFixture fixture = createFixture(SALES01_USER_ID, resolveDeptIdByCode("A"));
        seedInboundStock(fixture, 8);
        createAndApproveAllocation(fixture, 5, LocalDateTime.now().plusDays(30));

        String ownGroupPayload = """
                {
                  "goodsId": %d,
                  "skuId": %d,
                  "warehouseId": %d,
                  "stockTypeId": %d,
                  "quantity": 2,
                  "customerId": %d,
                  "groupCode": "A"
                }
                """.formatted(
                fixture.goods().getId(),
                fixture.sku().getId(),
                fixture.warehouse().getId(),
                fixture.stockType().getId(),
                fixture.customer().getId());

        mockMvc.perform(apiPost("/stock/group/customer/outbound")
                        .header("Authorization", "Bearer " + SALES01_TOKEN)
                        .header("Idempotency-Key", "own-group-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ownGroupPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        String otherGroupPayload = ownGroupPayload.replace("\"A\"", "\"B\"");

        mockMvc.perform(apiPost("/stock/group/customer/outbound")
                        .header("Authorization", "Bearer " + SALES01_TOKEN)
                        .header("Idempotency-Key", "other-group-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(otherGroupPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("users can only operate stock for their own department"));
    }

    private Long createAndApproveAllocation(TestFixture fixture, int quantity, LocalDateTime saleDeadline) {
        UserContext.setUserId(ADMIN_USER_ID);
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

        List<Long> orderIds = stockService.allocateToGroups(allocateDTO);
        assertEquals(1, orderIds.size());
        assertTrue(stockService.approveOrder(orderIds.get(0), true, "approve allocation"));
        return orderIds.get(0);
    }

    private void seedInboundStock(TestFixture fixture, int quantity) {
        String payload = """
                {
                  "goodsId": %d,
                  "skuId": %d,
                  "warehouseId": %d,
                  "stockTypeId": %d,
                  "quantity": %d,
                  "sourceType": 1
                }
                """.formatted(
                fixture.goods().getId(),
                fixture.sku().getId(),
                fixture.warehouse().getId(),
                fixture.stockType().getId(),
                quantity);
        try {
            mockMvc.perform(apiPost("/stock/inbound")
                            .header("Authorization", "Bearer " + ADMIN_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private TestFixture createFixture() {
        return createFixture(ADMIN_USER_ID, null);
    }

    private TestFixture createFixture(Long customerOwnerUserId, Long customerOwnerDeptId) {
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

        co.handk.backend.entity.Warehouse warehouse = new co.handk.backend.entity.Warehouse();
        warehouse.setName("warehouse-" + UUID.randomUUID());
        warehouse.setCode("WH-" + UUID.randomUUID());
        warehouse.setAddress("test");
        warehouse.setStatus(1);
        assertTrue(warehouseService.save(warehouse));

        co.handk.backend.entity.StockType stockType = new co.handk.backend.entity.StockType();
        stockType.setName("stock-type-" + UUID.randomUUID());
        stockType.setStatus(1);
        assertTrue(stockTypeService.save(stockType));

        Dept groupDept = resolveOrCreateGroupDept("A");

        Customer customer = new Customer();
        customer.setCustomerCode("CUS-" + UUID.randomUUID());
        customer.setName("customer-" + UUID.randomUUID());
        customer.setCountry("JP");
        customer.setOwnerUserId(customerOwnerUserId == null ? ADMIN_USER_ID : customerOwnerUserId);
        customer.setOwnerDeptId(customerOwnerDeptId == null ? groupDept.getId() : customerOwnerDeptId);
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

    private Long resolveDeptIdByCode(String code) {
        return resolveOrCreateGroupDept(code).getId();
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

    private MockHttpServletRequestBuilder apiPost(String path) {
        return post(API_CONTEXT_PATH + path).contextPath(API_CONTEXT_PATH);
    }

    private record TestFixture(
            Goods goods,
            GoodsSku sku,
            co.handk.backend.entity.Warehouse warehouse,
            co.handk.backend.entity.StockType stockType,
            Customer customer,
            Dept groupDept
    ) {
    }
}
