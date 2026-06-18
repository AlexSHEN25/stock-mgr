package co.handk.backend.service.impl;

import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.entity.*;
import co.handk.backend.mapper.StockMapper;
import co.handk.backend.mapper.StockRecordMapper;
import co.handk.backend.service.*;
import co.handk.backend.util.StringRedisUtil;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.GoodsImportConstant;
import co.handk.common.constant.RedisKey;
import co.handk.common.constant.StockBizConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.StockOperateDTO;
import co.handk.common.model.dto.create.StockOrderSubmitDTO;
import co.handk.common.model.dto.create.StockOrderSubmitItemDTO;
import co.handk.common.model.dto.create.StockGroupAllocateDTO;
import co.handk.common.model.dto.create.StockGroupAllocationItemDTO;
import co.handk.common.model.dto.query.StockQueryDTO;
import co.handk.common.model.dto.query.CustomerStockQueryDTO;
import co.handk.common.model.dto.update.UpdateStockDTO;
import co.handk.common.model.vo.StockVO;
import co.handk.common.model.vo.CustomerGoodsStockDetailVO;
import co.handk.common.model.vo.CustomerGoodsStockVO;
import co.handk.common.model.vo.CustomerGoodsMatrixCellVO;
import co.handk.common.model.vo.CustomerGoodsMatrixColumnVO;
import co.handk.common.model.vo.CustomerGoodsMatrixRowVO;
import co.handk.common.model.vo.CustomerGoodsMatrixVO;
import co.handk.common.model.vo.CustomerOutboundTreeNodeVO;
import co.handk.common.model.vo.CustomerStockSummaryVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class StockServiceImpl extends BaseServiceImpl<StockMapper, Stock, StockVO> implements StockService {

    private static final int MESSAGE_TYPE_INBOUND = 1;
    private static final int MESSAGE_TYPE_WARNING = 2;
    private static final int MESSAGE_IS_UNREAD = 0;
    private static final int MESSAGE_STATE_SENT = 1;
    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int EXPORT_COLUMN_WIDTH = 18 * 256;
    private static final long EXPORT_MAX_ROWS = 10_000L;
    private static final String EXPORT_SELF_STOCK_FILE_NAME = "self_stock_export.xlsx";
    private static final String EXPORT_SELF_STOCK_SHEET_NAME = "自社在庫一覧";
    private static final String ERROR_EXPORT_FAILED = "Excel出力に失敗しました";
    private static final DateTimeFormatter EXPORT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] SELF_STOCK_EXPORT_HEADERS = {
            "ID", "商品ID", "商品名", "SKU ID", "SKUコード", "倉庫ID", "倉庫名",
            "在庫区分ID", "在庫区分", "現在数量", "ロック数量", "価格", "通貨", "価格更新日時", "状態"
    };

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private GoodsSkuService goodsSkuService;
    @Autowired
    private StockTypeService stockTypeService;
    @Autowired
    private StockOrderService stockOrderService;
    @Autowired
    private StockOrderItemService stockOrderItemService;
    @Autowired
    private StockRecordService stockRecordService;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private PriceRecordService priceRecordService;
    @Autowired
    private PermissionQueryService permissionQueryService;
    @Autowired
    private StockBatchService stockBatchService;
    @Autowired
    private DeptService deptService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private RequestFormService requestFormService;
    @Autowired
    private StringRedisUtil stringRedisUtil;
    @Autowired
    private StockRecordMapper stockRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        return super.saveByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        if (!(dto instanceof UpdateStockDTO updateDto)) {
            return super.updateByDto(dto);
        }
        Stock existed = this.getByIdNotDeleted(updateDto.getId());
        if (existed == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫が存在しません");
        }

        java.math.BigDecimal oldPrice = existed.getPrice();
        java.math.BigDecimal newPrice = updateDto.getPrice();
        boolean priceChanged = newPrice != null && (oldPrice == null || oldPrice.compareTo(newPrice) != 0);
        if (priceChanged && updateDto.getPriceUpdateTime() == null) {
            updateDto.setPriceUpdateTime(LocalDateTime.now());
        }

        boolean updated = super.updateByDto(updateDto);
        if (!updated || !priceChanged) {
            return updated;
        }

        PriceRecord record = new PriceRecord();
        record.setGoodsId(Long.valueOf(updateDto.getGoodsId()));
        record.setGoodsName(updateDto.getGoodsName());
        record.setEnglishName(null);
        record.setSkuId(updateDto.getSkuId());
        record.setSkuCode(updateDto.getSkuCode());
        record.setOldPrice(oldPrice);
        record.setNewPrice(newPrice);
        record.setCurrency(updateDto.getCurrency());
        record.setDiscount(null);
        record.setPriceUpdateTime(updateDto.getPriceUpdateTime());
        Long operatorId = UserContext.getUserIdOrDefault();
        record.setOperatorId(operatorId);
        User operator = userService.getByIdNotDeleted(operatorId);
        record.setOperatorName(operator == null ? null : operator.getUsername());
        if (!priceRecordService.save(record)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "価格履歴の保存に失敗しました");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long inbound(StockOperateDTO dto) {
        Stock stock = resolveInboundStock(dto);
        Goods goods = requireGoods(stock.getGoodsId());
        GoodsSku sku = requireSku(stock.getSkuId(), goods.getId());
        String stockTypeName = getStockTypeName(stock.getStockTypeId());

        int scene = resolveInboundScene(dto.getSourceType());
        int beforeQty = safeInt(stock.getCurrentQty());
        int afterQty = beforeQty + dto.getQuantity();

        int sourceType = scene == StockBizConstant.INBOUND_SCENE_SELF
                ? StockBizConstant.SOURCE_TYPE_REQUEST : StockBizConstant.SOURCE_TYPE_MANUAL;
        boolean admin = permissionQueryService.isSuperAdmin(UserContext.getUserIdOrDefault());
        boolean needApprove = scene == StockBizConstant.INBOUND_SCENE_SELF && !admin;

        StockOrder order = saveStockOrder(stock, dto, StockBizConstant.ORDER_TYPE_INBOUND, sourceType,
                needApprove ? StockBizConstant.ORDER_STATE_APPROVING : StockBizConstant.ORDER_STATE_FINISHED, null);
        StockOrderItem item = saveOrderItem(order.getId(), goods, sku, stock, stockTypeName, dto, beforeQty, afterQty);
        if (!needApprove) {
            updateStockQuantityWithVersion(stock, afterQty);
            saveStockRecord(order, stock, dto.getRemark(), beforeQty, afterQty);
            stockBatchService.recordInbound(order, item, stock);
            notifyInbound(sku.getSkuCode(), dto.getQuantity(), afterQty, order.getId());
        }
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long outbound(StockOperateDTO dto) {
        Stock stock = resolveOutboundStock(dto);
        prepareOutboundAccess(dto, stock);
        requireAccessibleOutboundStock(stock, dto.getOutboundMode());
        Goods goods = requireGoods(stock.getGoodsId());
        GoodsSku sku = requireSku(stock.getSkuId(), goods.getId());
        String stockTypeName = getStockTypeName(stock.getStockTypeId());
        StockOrder existingOrder = findExistingOutboundOrder();
        if (existingOrder != null) {
            return existingOrder.getId();
        }
        String idempotencyKey = requireOutboundIdempotency(dto, stock);

        boolean groupCustomer = StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER
                .equals(normalizeOutboundMode(dto.getOutboundMode()));
        int beforeQty = groupCustomer
                ? stockBatchService.getGroupAvailableQty(dto.getDeptId(), Long.valueOf(stock.getGoodsId()),
                stock.getSkuId(), Long.valueOf(stock.getWarehouseId()), stock.getStockTypeId())
                : safeInt(stock.getCurrentQty()) - stockBatchService.getSelfLockedQty(stock.getId());
        if (beforeQty < dto.getQuantity()) {
            notifyInsufficientStock(sku.getSkuCode(), dto.getQuantity(), beforeQty, stock.getId());
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫数量が不足しています");
        }
        int afterQty = beforeQty - dto.getQuantity();

        StockOrder order = saveStockOrder(stock, dto, StockBizConstant.ORDER_TYPE_OUTBOUND,
                StockBizConstant.SOURCE_TYPE_MANUAL, StockBizConstant.ORDER_STATE_APPROVING, idempotencyKey);
        StockOrderItem item = saveOrderItem(order.getId(), goods, sku, stock, stockTypeName, dto, beforeQty, afterQty);
        stockBatchService.lockOutbound(order, item, stock);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> allocateToGroups(StockGroupAllocateDTO dto) {
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "自社在庫の組別配分は管理者のみ実行できます");
        }

        Stock stock = resolveAllocationStock(dto);
        java.util.LinkedHashMap<Long, GroupAllocationTarget> targets = new java.util.LinkedHashMap<>();
        int totalQty = 0;
        for (StockGroupAllocationItemDTO item : dto.getAllocations()) {
            Dept dept = resolveAccessibleGroupDept(
                    item.getDeptId(),
                    resolveRequestedGroupCode(item.getGroupCode(), item.getDeptCode()));
            int quantity = item.getQuantity();
            GroupAllocationTarget previous = targets.get(dept.getId());
            if (previous == null) {
                targets.put(dept.getId(), new GroupAllocationTarget(dept, quantity));
            } else {
                targets.put(dept.getId(), new GroupAllocationTarget(dept, previous.quantity() + quantity));
            }
            totalQty += quantity;
        }
        if (totalQty > safeInt(stock.getCurrentQty())) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "allocation quantity exceeds available self stock");
        }

        List<Long> orderIds = new ArrayList<>();
        for (GroupAllocationTarget target : targets.values()) {
            StockOperateDTO operation = new StockOperateDTO();
            operation.setStockId(stock.getId());
            operation.setGoodsId(stock.getGoodsId());
            operation.setSkuId(stock.getSkuId());
            operation.setWarehouseId(stock.getWarehouseId());
            operation.setStockTypeId(stock.getStockTypeId());
            operation.setQuantity(target.quantity());
            operation.setDeptId(target.dept().getId());
            operation.setGroupCode(target.dept().getCode());
            operation.setOutboundMode(StockBizConstant.OUTBOUND_MODE_GROUP_ALLOCATE);
            operation.setSaleDeadline(dto.getSaleDeadline());
            operation.setRemark(dto.getRemark());
            orderIds.add(outbound(operation));
        }
        return orderIds;
    }

    private Stock resolveAllocationStock(StockGroupAllocateDTO dto) {
        if (dto.getStockId() != null) {
            return requireStock(dto.getStockId(), dto.getWarehouseId());
        }
        StockOperateDTO operation = new StockOperateDTO();
        operation.setGoodsId(dto.getGoodsId());
        operation.setSkuId(dto.getSkuId());
        operation.setWarehouseId(dto.getWarehouseId());
        operation.setStockTypeId(dto.getStockTypeId());
        return resolveOutboundStock(operation);
    }

    private record GroupAllocationTarget(Dept dept, int quantity) {
    }

    @Override
    public <Q extends co.handk.common.model.PageQuery> PageResult<StockVO> page(Q dto) {
        if (!(dto instanceof StockQueryDTO query)) {
            return super.page(dto);
        }
        String scope = query.getStockScope() == null ? null : query.getStockScope().trim().toLowerCase();
        if ("self".equals(scope)) {
            query.setWarehouseId(requireWarehouseByCode("SELF").getId());
            return super.page(query);
        }
        if ("group".equals(scope)) {
            Dept dept = resolveGroupDeptForScope(query.getGroupCode());
            QueryWrapper<Stock> wrapper = buildGroupStockPageWrapper(query, dept);
            return pageGroupStockWithWrapper(query, wrapper, dept);
        }
        return super.page(query);
    }

    @Override
    public void exportSelfStock(StockQueryDTO query, HttpServletResponse response) {
        StockQueryDTO exportQuery = new StockQueryDTO();
        if (query != null) {
            BeanUtils.copyProperties(query, exportQuery);
        }
        exportQuery.setStockScope("self");
        exportQuery.setGroupCode(null);
        exportQuery.setPageNum(1L);
        exportQuery.setPageSize(EXPORT_MAX_ROWS);

        List<StockVO> records = page(exportQuery).getRecords();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(EXPORT_SELF_STOCK_SHEET_NAME);
            CellStyle headerStyle = createExportHeaderStyle(workbook);
            writeExportHeader(sheet, headerStyle, SELF_STOCK_EXPORT_HEADERS);
            for (int i = 0; i < records.size(); i++) {
                StockVO item = records.get(i);
                Row row = sheet.createRow(i + 1);
                int column = 0;
                writeExportCell(row, column++, item.getId());
                writeExportCell(row, column++, item.getGoodsId());
                writeExportCell(row, column++, item.getGoodsName());
                writeExportCell(row, column++, item.getSkuId());
                writeExportCell(row, column++, item.getSkuCode());
                writeExportCell(row, column++, item.getWarehouseId());
                writeExportCell(row, column++, item.getWarehouseName());
                writeExportCell(row, column++, item.getStockTypeId());
                writeExportCell(row, column++, item.getStockTypeName());
                writeExportCell(row, column++, item.getCurrentQty());
                writeExportCell(row, column++, item.getLockQty());
                writeExportCell(row, column++, item.getPrice());
                writeExportCell(row, column++, item.getCurrency());
                writeExportCell(row, column++, item.getPriceUpdateTime());
                writeExportCell(row, column, item.getStatusDesc());
            }
            writeWorkbookResponse(workbook, response, EXPORT_SELF_STOCK_FILE_NAME);
        } catch (IOException ex) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, ERROR_EXPORT_FAILED, ex);
        }
    }

    private CellStyle createExportHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor((short) 22);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private void writeExportHeader(Sheet sheet, CellStyle headerStyle, String[] headers) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, EXPORT_COLUMN_WIDTH);
        }
    }

    private void writeExportCell(Row row, int columnIndex, Object value) {
        Cell cell = row.createCell(columnIndex);
        if (value == null) {
            cell.setBlank();
            return;
        }
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
            return;
        }
        if (value instanceof LocalDateTime dateTime) {
            cell.setCellValue(EXPORT_DATE_TIME_FORMATTER.format(dateTime));
            return;
        }
        cell.setCellValue(String.valueOf(value));
    }

    private void writeWorkbookResponse(XSSFWorkbook workbook, HttpServletResponse response, String fileName)
            throws IOException {
        response.setContentType(GoodsImportConstant.EXCEL_CONTENT_TYPE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        workbook.write(response.getOutputStream());
        response.flushBuffer();
    }

    @Override
    public Integer getGroupAvailableQty(Long goodsId, Long skuId, Long warehouseId, Long stockTypeId,
                                        Long deptId, String groupCode) {
        Long userId = UserContext.getUserIdOrDefault();
        if (permissionQueryService.isSuperAdmin(userId)
                && deptId == null
                && (groupCode == null || groupCode.isBlank())) {
            return stockBatchService.getGroupAvailableQty(
                    null, goodsId, skuId, warehouseId, stockTypeId);
        }
        Dept dept = resolveAccessibleGroupDept(deptId, groupCode);
        return stockBatchService.getGroupAvailableQty(
                dept.getId(), goodsId, skuId, warehouseId, stockTypeId);
    }

    @Override
    public PageResult<CustomerStockSummaryVO> pageCustomerStock(CustomerStockQueryDTO query) {
        CustomerStockAccess access = resolveCustomerStockAccess(query);
        long total = stockRecordMapper.countCustomerSummaries(query, access.deptId(), access.ownerUserId());
        List<CustomerStockSummaryVO> records = total == 0
                ? List.of()
                : stockRecordMapper.selectCustomerSummaries(
                        query, access.deptId(), access.ownerUserId(), pageOffset(query), query.getPageSize());
        return PageResult.build(total, query.getPageNum(), query.getPageSize(), records);
    }

    @Override
    public PageResult<CustomerGoodsStockVO> pageCustomerGoodsStock(CustomerStockQueryDTO query) {
        CustomerStockAccess access = resolveCustomerGoodsAccess(query);
        long total = stockRecordMapper.countCustomerGoods(query, access.deptId(), access.ownerUserId());
        List<CustomerGoodsStockVO> records = total == 0
                ? List.of()
                : stockRecordMapper.selectCustomerGoods(
                        query, access.deptId(), access.ownerUserId(), pageOffset(query), query.getPageSize());
        return PageResult.build(total, query.getPageNum(), query.getPageSize(), records);
    }

    @Override
    public PageResult<CustomerGoodsStockDetailVO> pageCustomerGoodsStockDetails(CustomerStockQueryDTO query) {
        CustomerStockAccess access = resolveCustomerGoodsAccess(query);
        long total = stockRecordMapper.countCustomerGoodsDetails(query, access.deptId(), access.ownerUserId());
        List<CustomerGoodsStockDetailVO> records = total == 0
                ? List.of()
                : stockRecordMapper.selectCustomerGoodsDetails(
                        query, access.deptId(), access.ownerUserId(), pageOffset(query), query.getPageSize());
        return PageResult.build(total, query.getPageNum(), query.getPageSize(), records);
    }

    @Override
    public CustomerGoodsMatrixVO getCustomerGoodsMatrix(CustomerStockQueryDTO query) {
        CustomerStockAccess access = resolveCustomerGoodsAccess(query);
        long total = stockRecordMapper.countCustomerGoodsMatrixRows(
                query, access.deptId(), access.ownerUserId());
        List<CustomerGoodsMatrixColumnVO> columns = stockRecordMapper.selectCustomerGoodsMatrixColumns(
                query, access.deptId(), access.ownerUserId());
        List<CustomerGoodsMatrixRowVO> rows = total == 0
                ? List.of()
                : stockRecordMapper.selectCustomerGoodsMatrixRows(
                        query, access.deptId(), access.ownerUserId(), pageOffset(query), query.getPageSize());

        if (!rows.isEmpty()) {
            List<Long> goodsIds = rows.stream().map(CustomerGoodsMatrixRowVO::getGoodsId).toList();
            List<CustomerGoodsMatrixCellVO> cells = stockRecordMapper.selectCustomerGoodsMatrixCells(
                    query, access.deptId(), access.ownerUserId(), goodsIds);
            Map<Long, Map<Long, Long>> quantitiesByGoods = new LinkedHashMap<>();
            for (CustomerGoodsMatrixCellVO cell : cells) {
                quantitiesByGoods.computeIfAbsent(cell.getGoodsId(), ignored -> new LinkedHashMap<>())
                        .put(cell.getCustomerId(), cell.getQuantity());
            }
            for (CustomerGoodsMatrixRowVO row : rows) {
                Map<Long, Long> values = quantitiesByGoods.getOrDefault(row.getGoodsId(), Map.of());
                Map<String, Long> quantities = new LinkedHashMap<>();
                for (CustomerGoodsMatrixColumnVO column : columns) {
                    quantities.put(String.valueOf(column.getCustomerId()),
                            values.getOrDefault(column.getCustomerId(), 0L));
                }
                row.setQuantities(quantities);
            }
        }

        CustomerGoodsMatrixVO result = new CustomerGoodsMatrixVO();
        result.setTotal(total);
        result.setPageNum(query.getPageNum());
        result.setPageSize(query.getPageSize());
        result.setTotalPages((total + query.getPageSize() - 1) / query.getPageSize());
        result.setColumns(columns);
        result.setRows(rows);
        return result;
    }

    @Override
    public PageResult<CustomerOutboundTreeNodeVO> pageCustomerGoodsTree(CustomerStockQueryDTO query) {
        CustomerStockAccess access = resolveCustomerGoodsAccess(query);
        long total = stockRecordMapper.countCustomerGoodsTreeCountries(
                query, access.deptId(), access.ownerUserId());
        if (total == 0) {
            return PageResult.build(0L, query.getPageNum(), query.getPageSize(), List.of());
        }
        List<String> countries = stockRecordMapper.selectCustomerGoodsTreeCountries(
                query, access.deptId(), access.ownerUserId(), pageOffset(query), query.getPageSize());
        if (countries == null || countries.isEmpty()) {
            return PageResult.build(total, query.getPageNum(), query.getPageSize(), List.of());
        }
        List<CustomerOutboundTreeNodeVO> details = stockRecordMapper.selectCustomerGoodsTreeDetails(
                query, access.deptId(), access.ownerUserId(), countries);
        return PageResult.build(total, query.getPageNum(), query.getPageSize(), buildCustomerGoodsTree(details));
    }

    private CustomerStockAccess resolveCustomerStockAccess(CustomerStockQueryDTO query) {
        Long userId = UserContext.getUserIdOrDefault();
        if (permissionQueryService.isSuperAdmin(userId)) {
            return new CustomerStockAccess(null, null);
        }
        Long deptId = currentDeptId();
        if (deptId == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "現在のユーザーに部署が設定されていません");
        }
        query.setGroupCode(null);
        return new CustomerStockAccess(deptId, userId);
    }

    private CustomerStockAccess resolveCustomerGoodsAccess(CustomerStockQueryDTO query) {
        Long userId = UserContext.getUserIdOrDefault();
        if (permissionQueryService.isSuperAdmin(userId)) {
            return new CustomerStockAccess(null, null);
        }
        query.setGroupCode(null);
        return new CustomerStockAccess(null, userId);
    }

    private long pageOffset(CustomerStockQueryDTO query) {
        return (query.getPageNum() - 1L) * query.getPageSize();
    }

    private record CustomerStockAccess(Long deptId, Long ownerUserId) {
    }

    private List<CustomerOutboundTreeNodeVO> buildCustomerGoodsTree(List<CustomerOutboundTreeNodeVO> details) {
        Map<String, CustomerOutboundTreeNodeVO> countryNodes = new LinkedHashMap<>();
        Map<String, CustomerOutboundTreeNodeVO> customerNodes = new LinkedHashMap<>();
        if (details == null || details.isEmpty()) {
            return List.of();
        }

        for (CustomerOutboundTreeNodeVO detail : details) {
            String country = detail.getCountry() == null || detail.getCountry().isBlank()
                    ? "未設定" : detail.getCountry().trim();
            CustomerOutboundTreeNodeVO countryNode = countryNodes.computeIfAbsent(country, key -> {
                CustomerOutboundTreeNodeVO node = new CustomerOutboundTreeNodeVO();
                node.setId("country:" + key);
                node.setNodeType("COUNTRY");
                node.setCountry(key);
                node.setTotalQuantity(0);
                node.setGoodsKinds(0);
                node.setChildren(new ArrayList<>());
                return node;
            });

            String customerKey = country + ":" + detail.getCustomerId();
            CustomerOutboundTreeNodeVO customerNode = customerNodes.computeIfAbsent(customerKey, key -> {
                CustomerOutboundTreeNodeVO node = new CustomerOutboundTreeNodeVO();
                node.setId("customer:" + country + ":" + detail.getCustomerId());
                node.setNodeType("CUSTOMER");
                node.setCountry(country);
                node.setCustomerId(detail.getCustomerId());
                node.setCustomerName(detail.getCustomerName());
                node.setDeptId(detail.getDeptId());
                node.setGroupCode(detail.getGroupCode());
                node.setTotalQuantity(0);
                node.setGoodsKinds(0);
                node.setChildren(new ArrayList<>());
                countryNode.getChildren().add(node);
                return node;
            });

            CustomerOutboundTreeNodeVO recordNode = new CustomerOutboundTreeNodeVO();
            BeanUtils.copyProperties(detail, recordNode);
            recordNode.setId("record:" + detail.getRecordId());
            recordNode.setNodeType("RECORD");
            recordNode.setCountry(country);
            recordNode.setChildren(null);
            customerNode.getChildren().add(recordNode);

            int quantity = recordNode.getQuantity() == null ? 0 : recordNode.getQuantity();
            customerNode.setTotalQuantity(safeInt(customerNode.getTotalQuantity()) + quantity);
            customerNode.setGoodsKinds(safeInt(customerNode.getGoodsKinds()) + 1);
            countryNode.setTotalQuantity(safeInt(countryNode.getTotalQuantity()) + quantity);
            countryNode.setGoodsKinds(safeInt(countryNode.getGoodsKinds()) + 1);
        }

        return new ArrayList<>(countryNodes.values());
    }

    private Long currentDeptId() {
        User user = userService.getByIdNotDeleted(UserContext.getUserIdOrDefault());
        return user == null ? null : user.getDeptId();
    }

    private Dept resolveGroupDeptForScope(String groupCode) {
        Long userId = UserContext.getUserIdOrDefault();
        boolean admin = permissionQueryService.isSuperAdmin(userId);
        if (groupCode != null && !groupCode.isBlank()) {
            Dept requested = deptService.getOne(new QueryWrapper<Dept>()
                    .eq("code", groupCode.trim())
                    .last("LIMIT 1"));
            if (requested == null) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "部署が設定されていません: " + groupCode);
            }
            if (!admin) {
                Long userDeptId = currentDeptId();
                if (userDeptId == null || !userDeptId.equals(requested.getId())) {
                    throw new co.handk.backend.exception.BusinessException(
                            co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                            "自部署の在庫のみ参照できます");
                }
            }
            return requested;
        }
        if (admin) {
            return null;
        }
        return resolveAccessibleGroupDept(null);
    }

    private QueryWrapper<Stock> buildGroupStockPageWrapper(StockQueryDTO query, Dept dept) {
        String groupStockSql = "SELECT stock_id FROM t_group_stock"
                + " WHERE deleted = " + DeleteEnum.UNDELETED.getCode()
                + " AND current_qty > 0"
                + " AND state = " + StockBizConstant.BATCH_STATE_ACTIVE
                + " AND (sale_deadline IS NULL OR sale_deadline >= NOW())";
        if (dept != null) {
            groupStockSql += " AND dept_id = " + dept.getId();
        }
        QueryWrapper<Stock> wrapper = buildWrapper(query)
                .inSql("id", groupStockSql)
                .orderByDesc("update_time");
        return wrapper;
    }

    private PageResult<StockVO> pageGroupStockWithWrapper(
            StockQueryDTO query, QueryWrapper<Stock> wrapper, Dept dept) {
        Page<Stock> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Stock> result = this.page(page, wrapper);
        List<StockVO> records = result.getRecords().stream()
                .map(this::toVO)
                .peek(vo -> fillGroupStockScope(vo, dept))
                .peek(this::fillStatusDesc)
                .toList();
        fillStockJoins(records);
        return PageResult.build(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    private void fillGroupStockScope(StockVO vo, Dept dept) {
        Long deptId = dept == null ? null : dept.getId();
        vo.setDeptId(deptId);
        vo.setGroupCode(dept == null ? null : dept.getCode());
        vo.setCurrentQty(stockBatchService.getGroupAvailableQty(
                deptId,
                Long.valueOf(vo.getGoodsId()),
                vo.getSkuId(),
                Long.valueOf(vo.getWarehouseId()),
                vo.getStockTypeId()));
        vo.setLockQty(stockBatchService.getGroupLockedQty(
                deptId,
                Long.valueOf(vo.getGoodsId()),
                vo.getSkuId(),
                Long.valueOf(vo.getWarehouseId()),
                vo.getStockTypeId()));
    }

    private void fillStockJoins(List<StockVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (StockVO vo : records) {
            if (vo == null) {
                continue;
            }
            if (vo.getGoodsId() != null) {
                Goods goods = goodsService.getByIdNotDeleted(Long.valueOf(vo.getGoodsId()));
                vo.setGoodsName(goods == null ? null : goods.getName());
            }
            if (vo.getWarehouseId() != null) {
                Warehouse warehouse = warehouseService.getByIdNotDeleted(Long.valueOf(vo.getWarehouseId()));
                vo.setWarehouseName(warehouse == null ? null : warehouse.getName());
            }
            if (vo.getStockTypeId() != null) {
                StockType stockType = stockTypeService.getByIdNotDeleted(vo.getStockTypeId());
                vo.setStockTypeName(stockType == null ? null : stockType.getName());
            }
        }
    }

    private void prepareOutboundAccess(StockOperateDTO dto, Stock stock) {
        String mode = normalizeOutboundMode(dto.getOutboundMode());
        if (StockBizConstant.OUTBOUND_MODE_CUSTOMER.equals(mode)
                && dto.getCustomerId() != null
                && (dto.getDeptId() != null || hasText(dto.getGroupCode()) || hasText(dto.getDeptCode()))) {
            mode = StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER;
        }
        dto.setOutboundMode(mode);
        dto.setGroupCode(resolveRequestedGroupCode(dto.getGroupCode(), dto.getDeptCode()));
        Long userId = UserContext.getUserIdOrDefault();
        boolean admin = permissionQueryService.isSuperAdmin(userId);

        if (StockBizConstant.OUTBOUND_MODE_GROUP_ALLOCATE.equals(mode)) {
            if (!admin) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "自社在庫の組別配分は管理者のみ実行できます");
            }
            dto.setDeptId(resolveAccessibleGroupDept(dto.getDeptId(), dto.getGroupCode()).getId());
            return;
        }

        if (StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER.equals(mode)) {
            if (admin && dto.getDeptId() == null
                    && (dto.getGroupCode() == null || dto.getGroupCode().isBlank())) {
                dto.setDeptId(resolveUniqueGroupDeptId(stock.getId()));
            }
            dto.setDeptId(resolveAccessibleGroupDept(dto.getDeptId(), dto.getGroupCode()).getId());
            return;
        }

        if (!admin) {
            dto.setDeptId(null);
        }
    }

    private Long resolveUniqueGroupDeptId(Long stockId) {
        List<Long> deptIds = stockBatchService.getAvailableGroupDeptIds(stockId);
        if (deptIds.size() == 1) {
            return deptIds.get(0);
        }
        if (deptIds.isEmpty()) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "この在庫に利用可能な組別在庫がありません");
        }
        throw new co.handk.backend.exception.BusinessException(
                co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                "複数の組別在庫があります。部署IDまたは組コードを指定してください");
    }

    private void requireAccessibleOutboundStock(Stock stock, String outboundMode) {
        Long userId = UserContext.getUserIdOrDefault();
        if (permissionQueryService.isSuperAdmin(userId)
                || StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER.equals(outboundMode)) {
            return;
        }
        Warehouse selfWarehouse = requireWarehouseByCode("SELF");
        if (stock.getWarehouseId() == null || !selfWarehouse.getId().equals(Long.valueOf(stock.getWarehouseId()))) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "自社在庫または自部署の組別在庫のみ出庫申請できます");
        }
    }

    private Warehouse requireWarehouseByCode(String code) {
        Warehouse warehouse = warehouseService.getOne(new QueryWrapper<Warehouse>()
                .eq("code", code)
                .eq("status", StatusEnum.NOMAL.getCode())
                .last("LIMIT 1"));
        if (warehouse == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "必要な倉庫が設定されていません: " + code);
        }
        return warehouse;
    }

    private Dept resolveAccessibleGroupDept(Long requestedDeptId) {
        return resolveAccessibleGroupDept(requestedDeptId, null);
    }

    private Dept resolveAccessibleGroupDept(Long requestedDeptId, String requestedGroupCode) {
        Long userId = UserContext.getUserIdOrDefault();
        boolean admin = permissionQueryService.isSuperAdmin(userId);
        Long userDeptId = currentDeptId();
        Dept requestedGroupDept = findEnabledDeptByCode(requestedGroupCode);
        Long targetDeptId = requestedGroupDept != null
                ? requestedGroupDept.getId()
                : requestedDeptId;
        if (admin) {
            // Administrators may target any enabled stock-group department.
        } else {
            if (targetDeptId != null && !targetDeptId.equals(userDeptId)) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "自部署の在庫のみ操作できます");
            }
            targetDeptId = userDeptId;
        }
        if (targetDeptId == null && requestedGroupCode != null && !requestedGroupCode.isBlank()) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "部署が設定されていません: " + requestedGroupCode.trim().toUpperCase());
        }
        if (targetDeptId == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    admin
                            ? "組別在庫操作には部署IDまたは組コードが必要です"
                            : "現在のユーザーに部署が設定されていません");
        }
        Dept dept = deptService.getByIdNotDeleted(targetDeptId);
        if (dept == null || dept.getCode() == null || !isConfiguredGroupCode(dept.getCode())) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "部署が在庫組として設定されていません: " + (dept == null ? "null" : dept.getCode()));
        }
        return dept;
    }

    private Dept findEnabledDeptByCode(String groupCode) {
        if (groupCode == null || groupCode.isBlank()) {
            return null;
        }
        return deptService.getOne(new QueryWrapper<Dept>()
                .apply("UPPER(code) = {0}", groupCode.trim().toUpperCase())
                .eq("status", StatusEnum.NOMAL.getCode())
                .last("LIMIT 1"));
    }

    private String resolveRequestedGroupCode(String groupCode, String deptCode) {
        if (groupCode != null && !groupCode.isBlank()) {
            return groupCode;
        }
        return deptCode;
    }

    private boolean isConfiguredGroupCode(String deptCode) {
        return deptCode != null
                && permissionQueryService.getStockGroupCodes().contains(deptCode.trim().toUpperCase());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveOrder(Long orderId, Boolean approved, String approveRemark) {
        Long approverId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(approverId)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫伝票の承認は管理者のみ実行できます");
        }
        StockOrder order = stockOrderService.getByIdNotDeleted(orderId);
        if (order == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入庫伝票が存在しません");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_TYPE_INBOUND).equals(order.getOrderType())
                && !Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(order.getOrderType())) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入庫伝票ではありません");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_STATE_APPROVING).equals(order.getState())) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "承認待ち状態ではありません");
        }

        order.setApproverId(approverId);
        User approver = userService.getByIdNotDeleted(order.getApproverId());
        order.setApproverName(approver == null ? null : approver.getUsername());
        order.setApproveTime(LocalDateTime.now());
        order.setRemark(approveRemark);
        List<StockOrderItem> rejectItems = stockOrderItemService.list(new QueryWrapper<StockOrderItem>()
                .eq("order_id", orderId));
        if (rejectItems == null || rejectItems.isEmpty()) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入庫伝票明細が存在しません");
        }

        if (Boolean.FALSE.equals(approved)) {
            for (StockOrderItem item : rejectItems) {
                Stock stock = findStock(item.getGoodsId(), item.getSkuId(), order.getWarehouseId(), item.getStockTypeId());
                if (stock == null) {
                    throw new co.handk.backend.exception.BusinessException(
                            co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫商品が存在しません");
                }
                stockBatchService.releaseOutbound(order, item, stock);
            }
            order.setState(StockBizConstant.ORDER_STATE_CANCELED);
            if (!stockOrderService.updateById(order)) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "伝票更新に失敗しました");
            }
            return true;
        }

        List<StockOrderItem> items = stockOrderItemService.list(new QueryWrapper<StockOrderItem>()
                .eq("order_id", orderId));
        if (items == null || items.isEmpty()) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入庫伝票明細が存在しません");
        }

        for (StockOrderItem item : items) {
            Stock stock = findStock(item.getGoodsId(), item.getSkuId(), order.getWarehouseId(), item.getStockTypeId());
            if (stock == null) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫商品が存在しません");
            }

            int changeQty = safeInt(item.getChangeQty());
            boolean outboundOrder = Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(order.getOrderType());
            boolean groupAllocate = outboundOrder
                    && StockBizConstant.OUTBOUND_MODE_GROUP_ALLOCATE.equals(order.getOutboundMode());
            boolean groupCustomer = outboundOrder
                    && StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER.equals(order.getOutboundMode());
            int beforeQty = safeInt(item.getBeforeQty());
            int afterQty = safeInt(item.getAfterQty());
            if (Integer.valueOf(StockBizConstant.ORDER_TYPE_INBOUND).equals(order.getOrderType())) {
                beforeQty = safeInt(stock.getCurrentQty());
                afterQty = beforeQty + changeQty;
            }
            if (afterQty < 0) {
                notifyInsufficientStock(item.getSkuCode(), changeQty, beforeQty, stock.getId());
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "承認済みの出庫伝票に対する在庫が不足しているため、出庫処理を実行できません: 品番[" + item.getSkuCode() + "]");
            }
            if (outboundOrder) {
                stockBatchService.confirmOutbound(order, item, stock);
            } else {
                updateStockQuantityWithVersion(stock, afterQty);
            }
            item.setBeforeQty(beforeQty);
            item.setAfterQty(afterQty);
            if (!stockOrderItemService.updateById(item)) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫更新に失敗しました");
            }

            StockRecord record = new StockRecord();
            record.setBizNo(order.getOrderNo());
            record.setOrderId(order.getId());
            record.setOrderItemId(item.getId());
            record.setStockId(stock.getId());
            record.setGoodsId(item.getGoodsId());
            record.setSkuId(item.getSkuId());
            record.setSkuCode(item.getSkuCode());
            record.setGoodsName(item.getGoodsName());
            record.setEnglishName(item.getEnglishName());
            record.setBrandId(item.getBrandId());
            record.setBrandName(item.getBrandName());
            record.setSeriesId(item.getSeriesId());
            record.setSeriesName(item.getSeriesName());
            record.setCategoryId(item.getCategoryId());
            record.setCategoryName(item.getCategoryName());
            record.setStockTypeId(item.getStockTypeId());
            record.setStockTypeName(item.getStockTypeName());
            record.setMakerId(item.getMakerId());
            record.setMakerName(item.getMakerName());
            record.setWarehouseId(order.getWarehouseId());
            record.setBeforeQty(beforeQty);
            record.setChangeQty(item.getChangeQty());
            record.setAfterQty(afterQty);
            record.setOrderType(order.getOrderType());
            record.setSourceType(order.getSourceType());
            record.setPrice(item.getPrice());
            record.setCurrency(item.getCurrency());
            record.setPriceUpdateTime(stock.getPriceUpdateTime());
            record.setCustomerId(order.getCustomerId());
            record.setCustomerName(order.getCustomerName());
            record.setDeptId(order.getDeptId());
            record.setDeptCode(order.getDeptCode());
            record.setOutboundMode(order.getOutboundMode());
            record.setRequesterId(order.getRequesterId());
            record.setRequesterName(order.getRequesterName());
            record.setOperatorId(order.getOperatorId());
            record.setOperatorName(order.getOperatorName());
            record.setRemark(approveRemark);
            record.setBizDate(order.getBizDate());
            if (!stockRecordService.save(record)) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫履歴保存に失敗しました");
            }

            if (Integer.valueOf(StockBizConstant.ORDER_TYPE_INBOUND).equals(order.getOrderType())) {
                stockBatchService.recordInbound(order, item, stock);
                notifyInbound(item.getSkuCode(), changeQty, afterQty, order.getId());
            } else {
                notifyLowStock(item.getSkuCode(), afterQty, order.getId());
            }
        }

        order.setState(StockBizConstant.ORDER_STATE_FINISHED);
        order.setFinishTime(LocalDateTime.now());
        if (!stockOrderService.updateById(order)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "伝票更新に失敗しました");
        }
        if (Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(order.getOrderType())) {
            requestFormService.createFromOutbound(order,
                    items.stream().map(StockOrderItem::getId).toList(),
                    approveRemark);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitOrder(StockOrderSubmitDTO dto) {
        int orderType = dto.getOrderType() == null ? StockBizConstant.ORDER_TYPE_INBOUND : dto.getOrderType();
        if (orderType != StockBizConstant.ORDER_TYPE_INBOUND && orderType != StockBizConstant.ORDER_TYPE_OUTBOUND) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "伝票種別が不正です");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "明細は必須です");
        }

        List<OrderWorkingItem> workingItems = new ArrayList<>();
        int totalQty = 0;
        Long warehouseId = null;
        Long stockTypeId = null;
        String orderRemark = dto.getRemark();
        LocalDate bizDate = LocalDate.now();

        for (StockOrderSubmitItemDTO itemDTO : dto.getItems()) {
            Stock stock = requireStock(itemDTO.getStockId());
            if (orderType == StockBizConstant.ORDER_TYPE_OUTBOUND) {
                requireAccessibleOutboundStock(stock, StockBizConstant.OUTBOUND_MODE_CUSTOMER);
            }
            if (warehouseId == null) {
                warehouseId = Long.valueOf(stock.getWarehouseId());
            } else if (!warehouseId.equals(Long.valueOf(stock.getWarehouseId()))) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "同一伝票の倉庫は一致する必要があります");
            }
            if (stockTypeId == null) {
                stockTypeId = stock.getStockTypeId();
            }

            Goods goods = requireGoods(stock.getGoodsId());
            GoodsSku sku = requireSku(stock.getSkuId(), goods.getId());
            String stockTypeName = getStockTypeName(stock.getStockTypeId());

            int qty = safeInt(itemDTO.getQuantity());
            int beforeQty = safeInt(stock.getCurrentQty());
            int afterQty;
            if (orderType == StockBizConstant.ORDER_TYPE_INBOUND) {
                afterQty = beforeQty + qty;
            } else {
                if (beforeQty < qty) {
                    notifyInsufficientStock(sku.getSkuCode(), qty, beforeQty, stock.getId());
                    throw new co.handk.backend.exception.BusinessException(
                            co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                            "在庫数量が不足しています: SKU[" + sku.getSkuCode() + "]");
                }
                afterQty = beforeQty - qty;
            }

            OrderWorkingItem working = new OrderWorkingItem();
            working.stock = stock;
            working.goods = goods;
            working.sku = sku;
            working.stockTypeName = stockTypeName;
            working.changeQty = qty;
            working.beforeQty = beforeQty;
            working.afterQty = afterQty;
            working.remark = itemDTO.getRemark();
            workingItems.add(working);
            totalQty += qty;
        }

        Long userId = UserContext.getUserIdOrDefault();
        boolean admin = permissionQueryService.isSuperAdmin(userId);
        boolean selfInbound = orderType == StockBizConstant.ORDER_TYPE_INBOUND
                && resolveInboundScene(dto.getSourceType()) == StockBizConstant.INBOUND_SCENE_SELF;
        int sourceType = selfInbound ? StockBizConstant.SOURCE_TYPE_REQUEST : StockBizConstant.SOURCE_TYPE_MANUAL;
        boolean needApprove = orderType == StockBizConstant.ORDER_TYPE_OUTBOUND || (selfInbound && !admin);
        StockOrder order = new StockOrder();
        order.setOrderNo(generateOrderNo(orderType));
        order.setOrderType(orderType);
        order.setWarehouseId(warehouseId);
        order.setSourceType(sourceType);
        order.setTotalQty(totalQty);
        order.setStockTypeId(stockTypeId);
        order.setState(needApprove ? StockBizConstant.ORDER_STATE_APPROVING : StockBizConstant.ORDER_STATE_FINISHED);
        User user = userService.getByIdNotDeleted(userId);
        String username = user == null ? null : user.getUsername();
        order.setRequesterId(userId);
        order.setRequesterName(username);
        order.setOperatorId(userId);
        order.setOperatorName(username);
        order.setRemark(orderRemark);
        order.setBizDate(bizDate);
        if (!needApprove) {
            applyAutoApproval(order, userId, username);
        }
        if (!stockOrderService.save(order)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入出庫伝票の保存に失敗しました");
        }

        for (OrderWorkingItem working : workingItems) {
            StockOrderItem item = new StockOrderItem();
            item.setOrderId(order.getId());
            item.setGoodsId(working.goods.getId());
            item.setSkuId(working.sku.getId());
            item.setSkuCode(working.sku.getSkuCode());
            item.setGoodsName(working.goods.getName());
            item.setEnglishName(working.goods.getEnglishName());
            item.setBrandId(working.goods.getBrandId());
            item.setSeriesId(working.goods.getSeriesId());
            item.setCategoryId(working.goods.getCategoryId());
            item.setMakerId(working.goods.getMakerId());
            item.setStockTypeId(working.stock.getStockTypeId());
            item.setStockTypeName(working.stockTypeName);
            item.setBeforeQty(working.beforeQty);
            item.setChangeQty(working.changeQty);
            item.setAfterQty(working.afterQty);
            item.setPrice(working.stock.getPrice());
            item.setCurrency(working.stock.getCurrency() == null ? CommonConstant.DEFAULT_CURRENCY_JPY : working.stock.getCurrency());
            item.setRemark(working.remark == null ? orderRemark : working.remark);
            item.setBizDate(bizDate);
            if (!stockOrderItemService.save(item)) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入出庫明細の保存に失敗しました");
            }

            if (needApprove) {
                continue;
            }
            updateStockQuantityWithVersion(working.stock, working.afterQty);

            StockRecord record = new StockRecord();
            record.setBizNo(order.getOrderNo());
            record.setOrderId(order.getId());
            record.setOrderItemId(item.getId());
            record.setStockId(working.stock.getId());
            record.setGoodsId(item.getGoodsId());
            record.setSkuId(item.getSkuId());
            record.setSkuCode(item.getSkuCode());
            record.setGoodsName(item.getGoodsName());
            record.setEnglishName(item.getEnglishName());
            record.setBrandId(item.getBrandId());
            record.setBrandName(item.getBrandName());
            record.setSeriesId(item.getSeriesId());
            record.setSeriesName(item.getSeriesName());
            record.setCategoryId(item.getCategoryId());
            record.setCategoryName(item.getCategoryName());
            record.setStockTypeId(item.getStockTypeId());
            record.setStockTypeName(item.getStockTypeName());
            record.setMakerId(item.getMakerId());
            record.setMakerName(item.getMakerName());
            record.setWarehouseId(order.getWarehouseId());
            record.setBeforeQty(working.beforeQty);
            record.setChangeQty(working.changeQty);
            record.setAfterQty(working.afterQty);
            record.setOrderType(order.getOrderType());
            record.setSourceType(order.getSourceType());
            record.setPrice(item.getPrice());
            record.setCurrency(item.getCurrency());
            record.setPriceUpdateTime(working.stock.getPriceUpdateTime());
            record.setRequesterId(order.getRequesterId());
            record.setRequesterName(order.getRequesterName());
            record.setOperatorId(order.getOperatorId());
            record.setOperatorName(order.getOperatorName());
            record.setRemark(item.getRemark());
            record.setBizDate(order.getBizDate());
            if (!stockRecordService.save(record)) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫履歴の保存に失敗しました");
            }

            if (orderType == StockBizConstant.ORDER_TYPE_INBOUND) {
                notifyInbound(item.getSkuCode(), working.changeQty, working.afterQty, order.getId());
            } else {
                notifyLowStock(item.getSkuCode(), working.afterQty, order.getId());
            }
        }
        return order.getId();
    }

    private Stock requireStock(Long stockId) {
        Stock stock = this.getByIdNotDeleted(stockId);
        if (stock == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "処理に失敗しました");
        }
        return stock;
    }

    private Stock requireStock(Long stockId, Integer warehouseId) {
        Stock stock = requireStock(stockId);
        if (warehouseId != null && (stock.getWarehouseId() == null || !warehouseId.equals(stock.getWarehouseId()))) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "stock does not belong to requested warehouse");
        }
        return stock;
    }

    private Stock resolveInboundStock(StockOperateDTO dto) {
        if (dto.getStockId() != null) {
            return requireStock(dto.getStockId(), dto.getWarehouseId());
        }
        if (dto.getGoodsId() == null || dto.getSkuId() == null || dto.getWarehouseId() == null
                || dto.getStockTypeId() == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "goodsId, skuId, warehouseId and stockTypeId are required for inbound");
        }
        Goods goods = requireGoods(dto.getGoodsId());
        GoodsSku sku = requireSku(dto.getSkuId(), goods.getId());
        Stock existing = findStock(goods.getId(), sku.getId(), Long.valueOf(dto.getWarehouseId()), dto.getStockTypeId());
        if (existing != null) {
            return existing;
        }

        Stock stock = new Stock();
        stock.setGoodsId(dto.getGoodsId());
        stock.setGoodsName(goods.getName());
        stock.setSkuId(sku.getId());
        stock.setSkuCode(sku.getSkuCode());
        stock.setWarehouseId(dto.getWarehouseId());
        stock.setCurrentQty(0);
        stock.setPrice(sku.getPrice());
        stock.setCurrency(sku.getCurrency() == null ? CommonConstant.DEFAULT_CURRENCY_JPY : sku.getCurrency());
        stock.setPriceUpdateTime(sku.getPriceUpdateTime());
        stock.setStockTypeId(dto.getStockTypeId());
        stock.setStatus(StatusEnum.NOMAL.getCode());
        stock.setVersion(0L);
        if (!this.save(stock)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入庫用在庫の初期化に失敗しました");
        }
        return stock;
    }

    private Stock resolveOutboundStock(StockOperateDTO dto) {
        if (dto.getStockId() != null) {
            return requireStock(dto.getStockId(), dto.getWarehouseId());
        }
        if (dto.getGoodsId() == null || dto.getSkuId() == null || dto.getWarehouseId() == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "出庫には商品ID、SKU ID、倉庫IDが必要です");
        }
        Long warehouseId = Long.valueOf(dto.getWarehouseId());
        Stock stock = findStock(Long.valueOf(dto.getGoodsId()), dto.getSkuId(), warehouseId, dto.getStockTypeId());
        if (stock == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "出庫対象の在庫が見つかりません");
        }
        return stock;
    }

    private Goods requireGoods(Integer goodsId) {
        Goods goods = goodsService.getByIdNotDeleted(Long.valueOf(goodsId));
        if (goods == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "処理に失敗しました");
        }
        return goods;
    }

    private GoodsSku requireSku(Long skuId, Long goodsId) {
        GoodsSku sku = goodsSkuService.getByIdNotDeleted(skuId);
        if (sku == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "処理に失敗しました");
        }
        if (!goodsId.equals(sku.getGoodsId())) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "処理に失敗しました");
        }
        return sku;
    }

    private String getStockTypeName(Long stockTypeId) {
        if (stockTypeId == null) {
            return null;
        }
        StockType stockType = stockTypeService.getByIdNotDeleted(stockTypeId);
        return stockType == null ? null : stockType.getName();
    }

    private Stock findStock(Long goodsId, Long skuId, Long warehouseId, Long stockTypeId) {
        QueryWrapper<Stock> wrapper = new QueryWrapper<Stock>()
                .eq("goods_id", goodsId)
                .eq("sku_id", skuId)
                .eq("warehouse_id", warehouseId);
        if (stockTypeId == null) {
            wrapper.isNull("stock_type_id");
        } else {
            wrapper.eq("stock_type_id", stockTypeId);
        }
        return this.getOne(wrapper);
    }

    private StockOrder saveStockOrder(
            Stock stock, StockOperateDTO dto, int orderType, int sourceType, int state, String idempotencyKey) {
        StockOrder order = new StockOrder();
        order.setOrderNo(generateOrderNo(orderType));
        order.setOrderType(orderType);
        order.setWarehouseId(Long.valueOf(stock.getWarehouseId()));
        order.setSourceType(sourceType);
        order.setIdempotencyKey(idempotencyKey);
        order.setTotalQty(dto.getQuantity());
        order.setStockTypeId(stock.getStockTypeId());
        order.setState(state);

        Long userId = UserContext.getUserIdOrDefault();
        User user = userService.getByIdNotDeleted(userId);
        order.setRequesterId(userId);
        order.setRequesterName(user == null ? null : user.getUsername());
        order.setOperatorId(userId);
        order.setOperatorName(user == null ? null : user.getUsername());
        order.setRemark(dto.getRemark());
        order.setOutboundMode(normalizeOutboundMode(dto.getOutboundMode()));
        order.setCustomerId(dto.getCustomerId());
        if (dto.getCustomerId() != null) {
            Customer customer = customerService.getByIdNotDeleted(dto.getCustomerId());
            order.setCustomerName(customer == null ? dto.getCustomerName() : customer.getName());
        } else {
            order.setCustomerName(dto.getCustomerName());
        }
        Long targetDeptId = dto.getDeptId();
        if (StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER.equals(order.getOutboundMode()) && targetDeptId == null) {
            targetDeptId = user == null ? null : user.getDeptId();
        }
        order.setDeptId(targetDeptId);
        order.setSaleDeadline(dto.getSaleDeadline());
        if (targetDeptId != null) {
            Dept dept = deptService.getByIdNotDeleted(targetDeptId);
            order.setDeptCode(dept == null ? null : dept.getCode());
        }
        order.setBizDate(LocalDate.now());
        if (state == StockBizConstant.ORDER_STATE_FINISHED) {
            applyAutoApproval(order, userId, user == null ? null : user.getUsername());
        }
        if (!stockOrderService.save(order)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "処理に失敗しました");
        }
        return order;
    }

    private String normalizeOutboundMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return StockBizConstant.OUTBOUND_MODE_CUSTOMER;
        }
        if ("dept".equalsIgnoreCase(mode) || "group_allocate".equalsIgnoreCase(mode)) {
            return StockBizConstant.OUTBOUND_MODE_GROUP_ALLOCATE;
        }
        if ("group_customer".equalsIgnoreCase(mode)) {
            return StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER;
        }
        return StockBizConstant.OUTBOUND_MODE_CUSTOMER;
    }

    private void applyAutoApproval(StockOrder order, Long approverId, String approverName) {
        LocalDateTime now = LocalDateTime.now();
        order.setApproverId(approverId);
        order.setApproverName(approverName);
        order.setApproveTime(now);
        order.setFinishTime(now);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private StockOrderItem saveOrderItem(Long orderId,
                                         Goods goods,
                                         GoodsSku sku,
                                         Stock stock,
                                         String stockTypeName,
                                         StockOperateDTO dto,
                                         int beforeQty,
                                         int afterQty) {
        StockOrderItem item = new StockOrderItem();
        item.setOrderId(orderId);
        item.setGoodsId(goods.getId());
        item.setSkuId(sku.getId());
        item.setSkuCode(sku.getSkuCode());
        item.setGoodsName(goods.getName());
        item.setEnglishName(goods.getEnglishName());
        item.setBrandId(goods.getBrandId());
        item.setSeriesId(goods.getSeriesId());
        item.setCategoryId(goods.getCategoryId());
        item.setMakerId(goods.getMakerId());
        item.setStockTypeId(stock.getStockTypeId());
        item.setStockTypeName(stockTypeName);
        item.setBeforeQty(beforeQty);
        item.setChangeQty(dto.getQuantity());
        item.setAfterQty(afterQty);
        item.setPrice(stock.getPrice());
        item.setCurrency(stock.getCurrency() == null ? CommonConstant.DEFAULT_CURRENCY_JPY : stock.getCurrency());
        item.setRemark(dto.getRemark());
        item.setBizDate(LocalDate.now());
        if (!stockOrderItemService.save(item)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入出庫明細の保存に失敗しました");
        }
        return item;
    }

    private void saveStockRecord(StockOrder order, Stock stock, String remark, int beforeQty, int afterQty) {
        StockOrderItem item = stockOrderItemService.getOne(new QueryWrapper<StockOrderItem>()
                .eq("order_id", order.getId())
                .last("LIMIT 1"));
        if (item == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入出庫明細が存在しません");
        }
        StockRecord record = new StockRecord();
        record.setBizNo(order.getOrderNo());
        record.setOrderId(order.getId());
        record.setOrderItemId(item.getId());
        record.setStockId(stock.getId());
        record.setGoodsId(item.getGoodsId());
        record.setSkuId(item.getSkuId());
        record.setSkuCode(item.getSkuCode());
        record.setGoodsName(item.getGoodsName());
        record.setEnglishName(item.getEnglishName());
        record.setBrandId(item.getBrandId());
        record.setBrandName(item.getBrandName());
        record.setSeriesId(item.getSeriesId());
        record.setSeriesName(item.getSeriesName());
        record.setCategoryId(item.getCategoryId());
        record.setCategoryName(item.getCategoryName());
        record.setStockTypeId(item.getStockTypeId());
        record.setStockTypeName(item.getStockTypeName());
        record.setMakerId(item.getMakerId());
        record.setMakerName(item.getMakerName());
        record.setWarehouseId(Long.valueOf(stock.getWarehouseId()));
        record.setBeforeQty(beforeQty);
        record.setChangeQty(item.getChangeQty());
        record.setAfterQty(afterQty);
        record.setOrderType(order.getOrderType());
        record.setSourceType(order.getSourceType());
        record.setPrice(item.getPrice());
        record.setCurrency(item.getCurrency());
        record.setPriceUpdateTime(stock.getPriceUpdateTime());
        record.setRequesterId(order.getRequesterId());
        record.setRequesterName(order.getRequesterName());
        record.setOperatorId(order.getOperatorId());
        record.setOperatorName(order.getOperatorName());
        record.setRemark(remark);
        record.setBizDate(order.getBizDate());
        if (!stockRecordService.save(record)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫履歴保存に失敗しました");
        }
    }

    private String generateOrderNo(int orderType) {
        String prefix = orderType == StockBizConstant.ORDER_TYPE_INBOUND ? "IN" : "OUT";
        return prefix + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int resolveInboundScene(Integer sourceType) {
        int scene = sourceType == null ? StockBizConstant.INBOUND_SCENE_RESALE : sourceType;
        if (scene != StockBizConstant.INBOUND_SCENE_SELF && scene != StockBizConstant.INBOUND_SCENE_RESALE) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入庫元種別が不正です");
        }
        return scene;
    }

    private void updateStockQuantityWithVersion(Stock stock, int afterQty) {
        if (stock == null || stock.getId() == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "処理に失敗しました");
        }
        long oldVersion = stock.getVersion() == null ? 0L : stock.getVersion();
        boolean updated = this.update(new LambdaUpdateWrapper<Stock>()
                .eq(Stock::getId, stock.getId())
                .eq(Stock::getWarehouseId, stock.getWarehouseId())
                .eq(Stock::getVersion, oldVersion)
                .set(Stock::getCurrentQty, afterQty)
                .set(Stock::getVersion, oldVersion + 1));
        if (!updated) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "処理に失敗しました");
        }
        stock.setCurrentQty(afterQty);
        stock.setVersion(oldVersion + 1);
    }

    private static class OrderWorkingItem {
        private Stock stock;
        private Goods goods;
        private GoodsSku sku;
        private String stockTypeName;
        private int changeQty;
        private int beforeQty;
        private int afterQty;
        private String remark;
    }

    private void notifyInbound(String skuCode, int qty, int afterQty, Long sourceId) {
        String text = String.format("入庫完了: SKU[%s] 数量=%d, 在庫残=%d", skuCode, qty, afterQty);
        saveMessage(MESSAGE_TYPE_INBOUND, text, sourceId);
    }

    private void notifyInsufficientStock(String skuCode, int requestQty, int currentQty, Long sourceId) {
        String text = String.format("在庫不足: SKU[%s] 要求=%d, 現在庫=%d", skuCode, requestQty, currentQty);
        saveMessage(MESSAGE_TYPE_WARNING, text, sourceId);
    }

    private String requireOutboundIdempotency(StockOperateDTO dto, Stock stock) {
        String headerKey = currentIdempotencyKey();
        if (hasText(headerKey)) {
            String requestKey = "OUTBOUND:" + headerKey.trim();
            Long userId = UserContext.getUserIdOrDefault();
            String redisKey = RedisKey.IDEMPOTENCY_REQUEST + "stock:outbound:request:" + userId + ":" + requestKey;
            Boolean accepted = stringRedisUtil.setIfAbsent(redisKey, "1", 300L, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(accepted)) {
                StockOrder existing = findExistingOutboundOrder();
                if (existing != null) {
                    return requestKey;
                }
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "duplicate outbound request detected, please retry later");
            }
            return requestKey;
        }
        Long userId = UserContext.getUserIdOrDefault();
        String key = RedisKey.IDEMPOTENCY_REQUEST
                + "stock:outbound:"
                + userId + ":"
                + stock.getId() + ":"
                + stock.getGoodsId() + ":"
                + stock.getSkuId() + ":"
                + stock.getWarehouseId() + ":"
                + (stock.getStockTypeId() == null ? "null" : stock.getStockTypeId()) + ":"
                + dto.getQuantity() + ":"
                + (dto.getDeptId() == null ? "null" : dto.getDeptId()) + ":"
                + (dto.getCustomerId() == null ? "null" : dto.getCustomerId()) + ":"
                + normalizeOutboundMode(dto.getOutboundMode()) + ":"
                + (dto.getSaleDeadline() == null ? "null" : dto.getSaleDeadline());
        Boolean accepted = stringRedisUtil.setIfAbsent(key, "1", 300L, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(accepted)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "duplicate outbound request detected, please retry later");
        }
        return null;
    }

    private StockOrder findExistingOutboundOrder() {
        String headerKey = currentIdempotencyKey();
        if (!hasText(headerKey)) {
            return null;
        }
        Long userId = UserContext.getUserIdOrDefault();
        return stockOrderService.getOne(new QueryWrapper<StockOrder>()
                .eq("requester_id", userId)
                .eq("idempotency_key", "OUTBOUND:" + headerKey.trim())
                .eq("order_type", StockBizConstant.ORDER_TYPE_OUTBOUND)
                .orderByDesc("id")
                .last("LIMIT 1"));
    }

    private String currentIdempotencyKey() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        return request == null ? null : request.getHeader("Idempotency-Key");
    }

    private void notifyLowStock(String skuCode, int afterQty, Long sourceId) {
        if (afterQty > LOW_STOCK_THRESHOLD) {
            return;
        }
        String text = String.format("低在庫警告: SKU[%s] 在庫残=%d (閾値=%d)", skuCode, afterQty, LOW_STOCK_THRESHOLD);
        saveMessage(MESSAGE_TYPE_WARNING, text, sourceId);
    }

    private void saveMessage(int type, String messageText, Long sourceId) {
        Message message = new Message();
        message.setType(type);
        message.setUserId(UserContext.getUserIdOrDefault());
        message.setMessage(messageText);
        message.setSourceId(sourceId == null ? 0 : sourceId.intValue());
        message.setIsRead(MESSAGE_IS_UNREAD);
        message.setState(MESSAGE_STATE_SENT);
        messageService.save(message);
    }

    @Override
    protected <D> Stock toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Stock entity = new Stock();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    protected StockVO toVO(Stock entity) {
        if (entity == null) {
            return null;
        }
        StockVO vo = new StockVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setStockTypeName(getStockTypeName(entity.getStockTypeId()));
        java.util.Map<String, Integer> groupQty = stockBatchService.getGroupQuantities(entity.getId());
        vo.setGroupAQty(groupQty.getOrDefault("A", 0));
        vo.setGroupBQty(groupQty.getOrDefault("B", 0));
        vo.setGroupCQty(groupQty.getOrDefault("C", 0));
        return vo;
    }
}
