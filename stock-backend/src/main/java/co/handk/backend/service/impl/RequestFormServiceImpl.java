package co.handk.backend.service.impl;

import co.handk.backend.context.UserContext;
import co.handk.backend.entity.*;
import co.handk.backend.mapper.RequestFormMapper;
import co.handk.backend.mapper.StockMapper;
import co.handk.backend.service.*;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.StockBizConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.dto.create.CreateRequestFromOutboundDTO;
import co.handk.common.model.dto.create.CreateRequestFormDTO;
import co.handk.common.model.dto.update.RequestFormItemBatchDTO;
import co.handk.common.model.dto.update.UpdateRequestFormDTO;
import co.handk.common.model.vo.RequestCandidateItemVO;
import co.handk.common.model.vo.RequestFormVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RequestFormServiceImpl extends BaseServiceImpl<RequestFormMapper, RequestForm, RequestFormVO>
        implements RequestFormService {

    @Autowired private StockOrderService stockOrderService;
    @Autowired private StockOrderItemService stockOrderItemService;
    @Autowired private StockRecordService stockRecordService;
    @Autowired private RequestItemService requestItemService;
    @Autowired private UserService userService;
    @Autowired private DeptService deptService;
    @Autowired private StockMapper stockMapper;
    @Autowired private PermissionQueryService permissionQueryService;
    @Autowired private CustomerService customerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        if (dto instanceof CreateRequestFormDTO createDto) {
            Long userId = UserContext.getUserIdOrDefault();
            if (!permissionQueryService.isSuperAdmin(userId)) {
                createDto.setUserId(userId);
                User user = userService.getByIdNotDeleted(userId);
                if (user != null) {
                    createDto.setUsername(user.getUsername());
                    createDto.setDeptId(user.getDeptId());
                    Dept dept = user.getDeptId() == null ? null : deptService.getByIdNotDeleted(user.getDeptId());
                    createDto.setDeptName(dept == null ? null : dept.getName());
                }
            }
            validateCustomerOwnership(createDto.getCustomerId(), userId);
        }
        return super.saveByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        if (dto instanceof UpdateRequestFormDTO updateDto) {
            RequestForm existed = super.getByIdNotDeleted(updateDto.getId());
            requireOwned(existed);
            Long userId = UserContext.getUserIdOrDefault();
            if (!permissionQueryService.isSuperAdmin(userId)) {
                updateDto.setUserId(existed.getUserId());
                updateDto.setUsername(existed.getUsername());
                updateDto.setDeptId(existed.getDeptId());
                updateDto.setDeptName(existed.getDeptName());
            }
            validateCustomerOwnership(updateDto.getCustomerId(), userId);
        }
        return super.updateByDto(dto);
    }

    @Override
    protected <Q> QueryWrapper<RequestForm> buildWrapper(Q dto) {
        QueryWrapper<RequestForm> wrapper = super.buildWrapper(dto);
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId)) {
            wrapper.eq("user_id", userId);
        }
        return wrapper;
    }

    @Override
    public RequestForm getByIdNotDeleted(java.io.Serializable id) {
        RequestForm form = super.getByIdNotDeleted(id);
        requireOwned(form);
        return form;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIdLogic(Long id) {
        RequestForm form = super.getByIdNotDeleted(id);
        requireOwned(form);
        return super.deleteByIdLogic(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBatchLogic(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return DeleteEnum.UNDELETED.getCode();
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (permissionQueryService.isSuperAdmin(userId)) {
            return super.deleteBatchLogic(ids);
        }
        int rows = 0;
        for (Long id : ids) {
            RequestForm form = super.getByIdNotDeleted(id);
            requireOwned(form);
            rows += super.deleteByIdLogic(id);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromOutbound(CreateRequestFromOutboundDTO dto) {
        StockOrder outboundOrder = stockOrderService.getByIdNotDeleted(dto.getStockOrderId());
        if (outboundOrder == null) {
            throw new RuntimeException("出庫伝票が存在しません");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(outboundOrder.getOrderType())) {
            throw new RuntimeException("出庫伝票のみ申請作成できます");
        }

        Long loginUserId = UserContext.getUserIdOrDefault();
        if (!loginUserId.equals(outboundOrder.getRequesterId()) && !loginUserId.equals(outboundOrder.getOperatorId())) {
            throw new RuntimeException("自分の出庫伝票のみ申請できます");
        }

        List<StockOrderItem> allItems = stockOrderItemService.list(new QueryWrapper<StockOrderItem>()
                .eq("order_id", outboundOrder.getId())
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        if (allItems.isEmpty()) {
            throw new RuntimeException("申請対象の出庫明細がありません");
        }

        List<StockOrderItem> selectedItems = filterItems(allItems, dto.getStockOrderItemIds());
        if (selectedItems.isEmpty()) {
            throw new RuntimeException("申請対象の出庫明細がありません");
        }

        User user = userService.getByIdNotDeleted(loginUserId);
        Dept dept = user != null && user.getDeptId() != null ? deptService.getByIdNotDeleted(user.getDeptId()) : null;

        RequestForm form = new RequestForm();
        form.setBizNo(generateRequestNo());
        form.setUserId(loginUserId);
        form.setUsername(user == null ? null : user.getUsername());
        form.setDeptId(user == null ? null : user.getDeptId());
        form.setDeptName(dept == null ? null : dept.getName());
        form.setWarehouseId(outboundOrder.getWarehouseId());
        form.setSourceOrderId(outboundOrder.getId());
        form.setState(StockBizConstant.REQUEST_STATE_CREATED);
        form.setApproveRemark(dto.getRemark());

        int totalQty = 0;
        BigDecimal totalAmt = BigDecimal.ZERO;

        if (!this.save(form)) {
            throw new RuntimeException("申請書の保存に失敗しました");
        }

        for (StockOrderItem orderItem : selectedItems) {
            StockRecord stockRecord = stockRecordService.getOne(new QueryWrapper<StockRecord>()
                    .eq("order_id", outboundOrder.getId())
                    .eq("order_item_id", orderItem.getId())
                    .eq("order_type", StockBizConstant.ORDER_TYPE_OUTBOUND)
                    .eq("deleted", DeleteEnum.UNDELETED.getCode())
                    .last("LIMIT 1"));
            if (stockRecord == null) {
                throw new RuntimeException("source outbound stock record not found");
            }
            int requestQty = stockRecord.getChangeQty() == null ? 0 : stockRecord.getChangeQty();
            RequestItem requestItem = buildRequestItemFromStockRecord(form, stockRecord, dto.getRemark());
            requestItem.setRequestQty(requestQty);
            requestItem.setOutQty(requestQty);
            requestItem.setTotalAmt(safeAmount(stockRecord.getPrice()).multiply(BigDecimal.valueOf(requestQty)));
            applyOutboundRemainderDelta(stockRecord, requestQty);
            if (!requestItemService.save(requestItem)) {
                throw new RuntimeException("failed to save request item");
            }

            totalQty += requestQty;
            totalAmt = totalAmt.add(safeAmount(requestItem.getTotalAmt()));
        }
        form.setTotalQty(totalQty);
        form.setRequestQty(totalQty);
        form.setTotalAmt(totalAmt);
        if (!this.updateById(form)) {
            throw new RuntimeException("申請書の集計更新に失敗しました");
        }
        validateSourceBalance(form);
        return form.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reapplyInbound(Long requestId) {
        RequestForm form = this.getByIdNotDeleted(requestId);
        if (form == null) {
            throw new RuntimeException("申請書が存在しません");
        }

        List<RequestItem> items = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", requestId)
                .eq("state", StockBizConstant.REQUEST_ITEM_STATE_ADDED)
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        if (items.isEmpty()) {
            throw new RuntimeException("申請明細が存在しません");
        }

        Long loginUserId = UserContext.getUserIdOrDefault();

        StockOrder inboundOrder = new StockOrder();
        inboundOrder.setOrderNo(generateInboundNo());
        inboundOrder.setOrderType(StockBizConstant.ORDER_TYPE_INBOUND);
        inboundOrder.setWarehouseId(form.getWarehouseId());
        inboundOrder.setSourceType(StockBizConstant.SOURCE_TYPE_REQUEST);
        inboundOrder.setSourceId(form.getId());
        inboundOrder.setState(StockBizConstant.ORDER_STATE_APPROVING);
        inboundOrder.setRequesterId(loginUserId);
        inboundOrder.setRequesterName(form.getUsername());
        inboundOrder.setOperatorId(loginUserId);
        inboundOrder.setOperatorName(form.getUsername());
        inboundOrder.setRemark("申請書再入庫: " + form.getBizNo());
        inboundOrder.setBizDate(LocalDate.now());

        int totalQty = 0;
        for (RequestItem item : items) {
            totalQty += item.getRequestQty() == null ? 0 : item.getRequestQty();
        }
        inboundOrder.setTotalQty(totalQty);
        inboundOrder.setStockTypeId(items.get(0).getStockTypeId());
        if (!stockOrderService.save(inboundOrder)) {
            throw new RuntimeException("入庫伝票の保存に失敗しました");
        }

        for (RequestItem reqItem : items) {
            Stock stock = findStock(reqItem.getGoodsId(), reqItem.getSkuId(), reqItem.getWarehouseId(), reqItem.getStockTypeId());
            if (stock == null) {
                throw new RuntimeException("在庫商品が存在しません");
            }

            int beforeQty = stock.getCurrentQty() == null ? 0 : stock.getCurrentQty();
            int changeQty = reqItem.getRequestQty() == null ? 0 : reqItem.getRequestQty();
            int afterQty = beforeQty + changeQty;

            StockOrderItem orderItem = new StockOrderItem();
            orderItem.setOrderId(inboundOrder.getId());
            orderItem.setGoodsId(reqItem.getGoodsId());
            orderItem.setSkuId(reqItem.getSkuId());
            orderItem.setSkuCode(reqItem.getSkuCode());
            orderItem.setGoodsName(reqItem.getGoodsName());
            orderItem.setEnglishName(reqItem.getEnglishName());
            orderItem.setBrandId(reqItem.getBrandId());
            orderItem.setBrandName(reqItem.getBrandName());
            orderItem.setSeriesId(reqItem.getSeriesId());
            orderItem.setSeriesName(reqItem.getSeriesName());
            orderItem.setCategoryId(reqItem.getCategoryId());
            orderItem.setCategoryName(reqItem.getCategoryName());
            orderItem.setStockTypeId(reqItem.getStockTypeId());
            orderItem.setStockTypeName(reqItem.getStockTypeName());
            orderItem.setMakerId(reqItem.getMakerId());
            orderItem.setMakerName(reqItem.getMakerName());
            orderItem.setBeforeQty(beforeQty);
            orderItem.setChangeQty(changeQty);
            orderItem.setAfterQty(afterQty);
            orderItem.setPrice(reqItem.getPrice());
            orderItem.setCurrency(reqItem.getCurrency());
            orderItem.setRemark("申請書再入庫明細");
            orderItem.setBizDate(LocalDate.now());
            if (!stockOrderItemService.save(orderItem)) {
                throw new RuntimeException("入庫伝票明細の保存に失敗しました");
            }
        }

        form.setState(StockBizConstant.REQUEST_STATE_REINBOUND_APPLIED);
        if (!this.updateById(form)) {
            throw new RuntimeException("申請書状態の更新に失敗しました");
        }
        return inboundOrder.getId();
    }

        @Override
    public List<RequestCandidateItemVO> listCandidateItems(Long requestId) {
        RequestForm form = this.getByIdNotDeleted(requestId);
        if (form == null) {
            throw new RuntimeException("逕ｳ隲区嶌縺瑚ｦ九▽縺九ｊ縺ｾ縺帙ｓ");
        }
        requireOwned(form);

        List<StockRecord> records = listSourceOutboundRecords(form);
        if (records.isEmpty()) {
            return new ArrayList<>();
        }

        List<RequestItem> existing = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", requestId)
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        Map<Long, RequestItem> selectedMap = new HashMap<>();
        Map<String, RequestItem> selectedBySkuMap = new HashMap<>();
        for (RequestItem requestItem : existing) {
            if (!Integer.valueOf(StockBizConstant.REQUEST_ITEM_STATE_ADDED).equals(requestItem.getState())) {
                continue;
            }
            if (requestItem.getStockRecordId() != null) {
                selectedMap.put(requestItem.getStockRecordId(), requestItem);
            }
            if (requestItem.getGoodsId() != null && requestItem.getSkuId() != null) {
                selectedBySkuMap.put(requestItem.getGoodsId() + "_" + requestItem.getSkuId(), requestItem);
            }
        }

        List<RequestCandidateItemVO> result = new ArrayList<>();
        for (StockRecord record : records) {
            RequestItem selected = selectedMap.get(record.getId());
            if (selected == null && record.getGoodsId() != null && record.getSkuId() != null) {
                selected = selectedBySkuMap.get(record.getGoodsId() + "_" + record.getSkuId());
            }
            RequestCandidateItemVO vo = new RequestCandidateItemVO();
            vo.setStockRecordId(record.getId());
            vo.setStockOrderId(record.getOrderId());
            vo.setStockOrderItemId(record.getOrderItemId());
            vo.setOrderNo(record.getBizNo());
            vo.setOrderType(record.getOrderType());
            vo.setBizDate(record.getBizDate());
            vo.setGoodsId(record.getGoodsId());
            vo.setSkuId(record.getSkuId());
            vo.setSkuCode(record.getSkuCode());
            vo.setGoodsName(record.getGoodsName());
            vo.setBrandName(record.getBrandName());
            vo.setSeriesName(record.getSeriesName());
            vo.setCategoryName(record.getCategoryName());
            vo.setStockTypeName(record.getStockTypeName());
            vo.setMakerName(record.getMakerName());
            StockOrderItem orderItem = stockOrderItemService.getByIdNotDeleted(record.getOrderItemId());
            int remainingQty = orderItem == null || orderItem.getChangeQty() == null ? 0 : Math.abs(orderItem.getChangeQty());
            int selectedQty = 0;
            if (selected != null) {
                if (selected.getRequestQty() != null) {
                    selectedQty = Math.abs(selected.getRequestQty());
                } else {
                    int originalQty = record.getChangeQty() == null ? 0 : Math.abs(record.getChangeQty());
                    selectedQty = Math.max(0, originalQty - remainingQty);
                }
            }
            vo.setChangeQty(remainingQty + selectedQty);
            vo.setPrice(record.getPrice());
            vo.setCurrency(record.getCurrency());
            vo.setSelected(selected != null);
            vo.setRequestItemState(selected == null ? null : selected.getState());
            vo.setRequestItemId(selected == null ? null : selected.getId());
            result.add(vo);
        }
        return result;
    }

        @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addItemsFromStockOrder(RequestFormItemBatchDTO dto) {
        RequestForm form = this.getByIdNotDeleted(dto.getRequestId());
        if (form == null) {
            throw new RuntimeException("request form not found");
        }
        requireOwned(form);
        Map<Long, Integer> requestedQuantities = resolveRequestedQuantities(dto);
        if (requestedQuantities.isEmpty()) {
            return true;
        }
        requireOwnedSourceOutbound(form);
        for (Map.Entry<Long, Integer> entry : requestedQuantities.entrySet()) {
            Long stockRecordId = entry.getKey();
            int requestedQty = entry.getValue();
            StockRecord stockRecord = requireSourceStockRecord(form, stockRecordId);

            RequestItem existing = requestItemService.getOne(new QueryWrapper<RequestItem>()
                    .eq("request_id", form.getId())
                    .eq("stock_record_id", stockRecordId)
                    .eq("deleted", DeleteEnum.UNDELETED.getCode())
                    .last("LIMIT 1"));
            if (existing == null) {
                existing = requestItemService.getOne(new QueryWrapper<RequestItem>()
                        .eq("request_id", form.getId())
                        .eq("goods_id", stockRecord.getGoodsId())
                        .eq("sku_id", stockRecord.getSkuId())
                        .eq("state", StockBizConstant.REQUEST_ITEM_STATE_ADDED)
                        .eq("deleted", DeleteEnum.UNDELETED.getCode())
                        .last("LIMIT 1"));
            }

            int currentQty = existing == null || !Integer.valueOf(StockBizConstant.REQUEST_ITEM_STATE_ADDED).equals(existing.getState())
                    || existing.getRequestQty() == null ? 0 : Math.abs(existing.getRequestQty());
            StockOrderItem sourceOrderItem = stockOrderItemService.getByIdNotDeleted(stockRecord.getOrderItemId());
            int remainingQty = sourceOrderItem == null || sourceOrderItem.getChangeQty() == null
                    ? 0 : Math.abs(sourceOrderItem.getChangeQty());
            int originalQty = stockRecord.getChangeQty() == null ? 0 : Math.abs(stockRecord.getChangeQty());
            int maxRequestQty = Math.max(remainingQty + currentQty, originalQty);
            if (requestedQty > maxRequestQty) {
                throw new RuntimeException("request qty cannot be greater than outbound qty");
            }
            int deltaQty = requestedQty - currentQty;
            applyOutboundRemainderDelta(stockRecord, deltaQty);

            if (existing == null) {
                RequestItem requestItem = buildRequestItemFromStockRecord(form, stockRecord, dto.getRemark());
                requestItem.setRequestQty(requestedQty);
                requestItem.setOutQty(requestedQty);
                requestItem.setTotalAmt(safeAmount(stockRecord.getPrice()).multiply(BigDecimal.valueOf(requestedQty)));
                if (!saveOrReactivateRequestItem(form.getId(), stockRecordId, requestItem, dto.getRemark())) {
                    throw new RuntimeException("failed to add request item");
                }
            } else {
                existing.setState(StockBizConstant.REQUEST_ITEM_STATE_ADDED);
                existing.setRequestQty(requestedQty);
                existing.setOutQty(requestedQty);
                existing.setPrice(stockRecord.getPrice());
                existing.setCurrency(stockRecord.getCurrency());
                existing.setTotalAmt(safeAmount(stockRecord.getPrice()).multiply(BigDecimal.valueOf(requestedQty)));
                existing.setStockRecordId(stockRecord.getId());
                if (dto.getRemark() != null && !dto.getRemark().isBlank()) {
                    existing.setRemark(dto.getRemark());
                }
                if (!requestItemService.updateById(existing)) {
                    throw new RuntimeException("failed to update request item");
                }
            }
        }

        validateKnifeHandleQuantity(form.getId());
        validateSourceBalance(form);
        recalculateRequestFormSummary(form.getId());
        return true;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeItemsFromRequest(RequestFormItemBatchDTO dto) {
        RequestForm form = this.getByIdNotDeleted(dto.getRequestId());
        if (form == null) {
            throw new RuntimeException("request form not found");
        }
        requireOwned(form);
        requireOwnedSourceOutbound(form);
        List<Long> stockRecordIds = !normalizeStockRecordIds(dto.getStockOrderItemIds()).isEmpty()
                ? normalizeStockRecordIds(dto.getStockOrderItemIds())
                : new ArrayList<>(resolveRequestedQuantities(dto).keySet());
        if (stockRecordIds.isEmpty()) {
            return true;
        }

        List<RequestItem> existing = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", form.getId())
                .in("stock_record_id", stockRecordIds)
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        if (existing == null || existing.isEmpty()) {
            return true;
        }

        for (RequestItem requestItem : existing) {
            int currentQty = requestItem.getRequestQty() == null ? 0 : Math.abs(requestItem.getRequestQty());
            StockRecord record = requireSourceStockRecord(form, requestItem.getStockRecordId());
            applyOutboundRemainderDelta(record, -currentQty);
            requestItem.setState(StockBizConstant.REQUEST_ITEM_STATE_REMOVED);
            if (!requestItemService.updateById(requestItem)) {
                throw new RuntimeException("failed to remove request item");
            }
        }
        validateSourceBalance(form);
        recalculateRequestFormSummary(form.getId());
        return true;
    }
@Override
    public void downloadBDeptRequestForm(Long requestId, HttpServletResponse response) {
        RequestForm form = this.getByIdNotDeleted(requestId);
        if (form == null) {
            throw new RuntimeException("申請書が存在しません");
        }
        List<RequestItem> items = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", requestId)
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("申請明細が存在しません");
        }
        Customer customer = form.getCustomerId() == null ? null : customerService.getByIdNotDeleted(form.getCustomerId());

        try (InputStream templateInput = openTemplateInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(templateInput)) {
            Sheet sheet = workbook.getSheetAt(0);
            workbook.setSheetName(0, safeSheetName(form.getBizNo()));
            while (workbook.getNumberOfSheets() > 1) {
                workbook.removeSheetAt(workbook.getNumberOfSheets() - 1);
            }
            fillHeader(sheet, customer);
            fillItems(sheet, items);

            String filename = "request_" + form.getBizNo() + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            workbook.write(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException("申請書テンプレートの出力に失敗しました: " + e.getMessage(), e);
        }
    }

    private InputStream openTemplateInputStream() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("template/request_form_template.xlsx");
        if (classPathResource.exists()) {
            return classPathResource.getInputStream();
        }
        throw new IOException("テンプレートが見つかりません: classpath:template/request_form_template.xlsx");
    }

    private void fillHeader(Sheet sheet, Customer customer) {
        setCellText(sheet, 5, 7, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-M-d")));
        if (customer != null) {
            setCellText(sheet, 5, 1, "MESSRS: " + safe(customer.getName()));
            setCellText(sheet, 6, 1, "Address: " + safe(customer.getAddress()));
            setCellText(sheet, 7, 1, "Tel: " + safe(customer.getPhone()));
            setCellText(sheet, 8, 1, "EMAIL: " + safe(customer.getEmail()));
        }
    }

    private void fillItems(Sheet sheet, List<RequestItem> items) {
        int startRow = 22;
        int clearToRow = 499;
        for (int r = startRow; r <= clearToRow; r++) {
            for (int c = 1; c <= 9; c++) {
                setCellText(sheet, r, c, "");
            }
        }
        for (int i = 0; i < items.size(); i++) {
            RequestItem item = items.get(i);
            int row = startRow + i;
            setCellText(sheet, row, 1, String.valueOf(i + 1));
            setCellText(sheet, row, 2, safe(item.getBrandName()));
            setCellText(sheet, row, 3, safe(item.getGoodsName()));
            setCellText(sheet, row, 4, String.valueOf(item.getRequestQty() == null ? 0 : item.getRequestQty()));
            setCellText(sheet, row, 5, formatCurrency(item.getCurrency(), item.getPrice()));
            setCellText(sheet, row, 6, formatCurrency(item.getCurrency(), item.getTotalAmt()));
            setCellText(sheet, row, 7, safe(item.getRemark()));
            setCellText(sheet, row, 9, safe(item.getSkuCode()));
        }
    }

    private void setCellText(Sheet sheet, int rowIndex, int colIndex, String value) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex, CellType.STRING);
        } else {
            cell.setCellType(CellType.STRING);
        }
        cell.setCellValue(value == null ? "" : value);
    }

    private String formatCurrency(String currency, BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        String code = (currency == null || currency.isBlank()) ? CommonConstant.DEFAULT_CURRENCY_JPY : currency;
        return code + " " + amount.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeSheetName(String value) {
        String base = (value == null || value.isBlank()) ? "RequestForm" : value;
        String sanitized = base
                .replace('\\', '_')
                .replace('/', '_')
                .replace('*', '_')
                .replace('[', '_')
                .replace(']', '_')
                .replace(':', '_')
                .replace('?', '_');
        return sanitized.length() > 31 ? sanitized.substring(0, 31) : sanitized;
    }

    private List<StockOrderItem> filterItems(List<StockOrderItem> allItems, List<Long> selectedIds) {
        if (selectedIds == null || selectedIds.isEmpty()) {
            return allItems;
        }
        List<StockOrderItem> selected = new ArrayList<>();
        for (StockOrderItem item : allItems) {
            if (selectedIds.contains(item.getId())) {
                selected.add(item);
            }
        }
        return selected;
    }

    
    private List<Long> normalizeStockRecordIds(List<Long> ids) {
        java.util.LinkedHashSet<Long> uniq = new java.util.LinkedHashSet<>();
        if (ids != null) {
            for (Long id : ids) {
                if (id != null && id > 0) {
                    uniq.add(id);
                }
            }
        }
        return new ArrayList<>(uniq);
    }

    private Map<Long, Integer> resolveRequestedQuantities(RequestFormItemBatchDTO dto) {
        Map<Long, Integer> quantities = new java.util.LinkedHashMap<>();
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (RequestFormItemBatchDTO.Item item : dto.getItems()) {
                if (item == null || item.getStockRecordId() == null || item.getStockRecordId() <= 0) {
                    continue;
                }
                int qty = item.getRequestQty() == null ? 0 : item.getRequestQty();
                if (qty > 0) {
                    quantities.merge(item.getStockRecordId(), qty, Integer::sum);
                }
            }
            return quantities;
        }
        for (Long stockRecordId : normalizeStockRecordIds(dto.getStockOrderItemIds())) {
            StockRecord record = stockRecordService.getByIdNotDeleted(stockRecordId);
            if (record != null && record.getChangeQty() != null && record.getChangeQty() > 0) {
                quantities.put(stockRecordId, record.getChangeQty());
            }
        }
        return quantities;
    }

    private StockOrder requireOwnedSourceOutbound(RequestForm form) {
        if (form.getSourceOrderId() == null) {
            throw new RuntimeException("request form source outbound order is required");
        }
        StockOrder order = stockOrderService.getByIdNotDeleted(form.getSourceOrderId());
        if (order == null || !Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(order.getOrderType())) {
            throw new RuntimeException("source outbound order not found");
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (permissionQueryService.isSuperAdmin(userId)) {
            return order;
        }
        if (!userId.equals(order.getRequesterId()) && !userId.equals(order.getOperatorId())) {
            throw new RuntimeException("source outbound order is not owned by current user");
        }
        return order;
    }

    private StockRecord requireSourceStockRecord(RequestForm form, Long stockRecordId) {
        StockRecord record = stockRecordService.getByIdNotDeleted(stockRecordId);
        if (record == null || !form.getSourceOrderId().equals(record.getOrderId())
                || !Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(record.getOrderType())) {
            throw new RuntimeException("selected stock record is not available");
        }
        return record;
    }

    private void applyOutboundRemainderDelta(StockRecord record, int deltaQty) {
        if (deltaQty == 0) {
            return;
        }
        StockOrderItem orderItem = stockOrderItemService.getByIdNotDeleted(record.getOrderItemId());
        if (orderItem == null) {
            throw new RuntimeException("source outbound item not found");
        }
        int originalQty = record.getChangeQty() == null ? 0 : Math.abs(record.getChangeQty());
        int currentQty = orderItem.getChangeQty() == null ? 0 : Math.abs(orderItem.getChangeQty());
        int nextQty = currentQty - deltaQty;
        if (nextQty < 0 || nextQty > originalQty) {
            throw new RuntimeException("request quantity exceeds source outbound remaining quantity");
        }
        int affected = stockOrderItemService.getBaseMapper().update(
                null,
                new LambdaUpdateWrapper<StockOrderItem>()
                        .eq(StockOrderItem::getId, orderItem.getId())
                        .eq(StockOrderItem::getDeleted, DeleteEnum.UNDELETED.getCode())
                        .eq(StockOrderItem::getChangeQty, currentQty)
                        .set(StockOrderItem::getChangeQty, nextQty)
        );
        if (affected <= 0) {
            throw new RuntimeException("source outbound item changed concurrently, please retry");
        }
        StockOrder order = stockOrderService.getByIdNotDeleted(record.getOrderId());
        if (order != null) {
            int totalQty = order.getTotalQty() == null ? 0 : order.getTotalQty();
            int orderAffected = stockOrderService.getBaseMapper().update(
                    null,
                    new LambdaUpdateWrapper<StockOrder>()
                            .eq(StockOrder::getId, order.getId())
                            .eq(StockOrder::getDeleted, DeleteEnum.UNDELETED.getCode())
                            .eq(StockOrder::getTotalQty, totalQty)
                            .set(StockOrder::getTotalQty, totalQty - deltaQty)
            );
            if (orderAffected <= 0) {
                throw new RuntimeException("source outbound order changed concurrently, please retry");
            }
        }
    }

    private void validateSourceBalance(RequestForm form) {
        List<StockRecord> records = listSourceOutboundRecords(form);
        for (StockRecord record : records) {
            int originalQty = record.getChangeQty() == null ? 0 : record.getChangeQty();
            StockOrderItem orderItem = stockOrderItemService.getByIdNotDeleted(record.getOrderItemId());
            int remainingQty = orderItem == null || orderItem.getChangeQty() == null ? 0 : orderItem.getChangeQty();
            RequestItem requestItem = requestItemService.getOne(new QueryWrapper<RequestItem>()
                    .eq("request_id", form.getId())
                    .eq("stock_record_id", record.getId())
                    .eq("state", StockBizConstant.REQUEST_ITEM_STATE_ADDED)
                    .eq("deleted", DeleteEnum.UNDELETED.getCode())
                    .last("LIMIT 1"));
            int requestQty = requestItem == null || requestItem.getRequestQty() == null ? 0 : Math.abs(requestItem.getRequestQty());
            if (requestQty + remainingQty != originalQty) {
                throw new RuntimeException("request and outbound quantities are inconsistent");
            }
        }
    }

    private boolean saveOrReactivateRequestItem(Long requestId, Long stockRecordId, RequestItem candidate, String remark) {
        try {
            return requestItemService.save(candidate);
        } catch (Exception ex) {
            RequestItem existed = requestItemService.getOne(new QueryWrapper<RequestItem>()
                    .eq("request_id", requestId)
                    .eq("stock_record_id", stockRecordId)
                    .eq("deleted", DeleteEnum.UNDELETED.getCode())
                    .last("LIMIT 1"));
            if (existed == null) {
                throw ex;
            }
            existed.setState(StockBizConstant.REQUEST_ITEM_STATE_ADDED);
            existed.setRequestQty(candidate.getRequestQty());
            existed.setOutQty(candidate.getOutQty());
            existed.setPrice(candidate.getPrice());
            existed.setCurrency(candidate.getCurrency());
            existed.setTotalAmt(candidate.getTotalAmt());
            if (remark != null && !remark.isBlank()) {
                existed.setRemark(remark);
            }
            return requestItemService.updateById(existed);
        }
    }
    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private RequestItem buildRequestItemFromOrder(RequestForm form, StockOrderItem orderItem, String remark) {
        RequestItem requestItem = new RequestItem();
        requestItem.setRequestId(form.getId());
        requestItem.setGoodsId(orderItem.getGoodsId());
        requestItem.setSkuId(orderItem.getSkuId());
        requestItem.setSkuCode(orderItem.getSkuCode());
        requestItem.setGoodsName(orderItem.getGoodsName());
        requestItem.setEnglishName(orderItem.getEnglishName());
        requestItem.setBrandId(orderItem.getBrandId());
        requestItem.setBrandName(orderItem.getBrandName());
        requestItem.setSeriesId(orderItem.getSeriesId());
        requestItem.setSeriesName(orderItem.getSeriesName());
        requestItem.setCategoryId(orderItem.getCategoryId());
        requestItem.setCategoryName(orderItem.getCategoryName());
        requestItem.setStockTypeId(orderItem.getStockTypeId());
        requestItem.setStockTypeName(orderItem.getStockTypeName());
        requestItem.setMakerId(orderItem.getMakerId());
        requestItem.setMakerName(orderItem.getMakerName());
        requestItem.setWarehouseId(form.getWarehouseId());
        requestItem.setPrice(orderItem.getPrice());
        requestItem.setExchangeRate(BigDecimal.ONE);
        requestItem.setCurrency(orderItem.getCurrency() == null ? CommonConstant.DEFAULT_CURRENCY_JPY : orderItem.getCurrency());
        requestItem.setDiscount(BigDecimal.ONE);
        requestItem.setRequestQty(orderItem.getChangeQty());
        requestItem.setApproveQty(0);
        requestItem.setOutQty(orderItem.getChangeQty());
        requestItem.setTotalAmt(safeAmount(orderItem.getPrice())
                .multiply(BigDecimal.valueOf(orderItem.getChangeQty() == null ? 0 : orderItem.getChangeQty())));
        requestItem.setStockRecordId(null);
        requestItem.setState(StockBizConstant.REQUEST_ITEM_STATE_ADDED);
        requestItem.setRemark(remark);
        return requestItem;
    }

    private RequestItem buildRequestItemFromStockRecord(RequestForm form, StockRecord record, String remark) {
        RequestItem requestItem = new RequestItem();
        requestItem.setRequestId(form.getId());
        requestItem.setGoodsId(record.getGoodsId());
        requestItem.setSkuId(record.getSkuId());
        requestItem.setSkuCode(record.getSkuCode());
        requestItem.setGoodsName(record.getGoodsName());
        requestItem.setEnglishName(record.getEnglishName());
        requestItem.setBrandId(record.getBrandId());
        requestItem.setBrandName(record.getBrandName());
        requestItem.setSeriesId(record.getSeriesId());
        requestItem.setSeriesName(record.getSeriesName());
        requestItem.setCategoryId(record.getCategoryId());
        requestItem.setCategoryName(record.getCategoryName());
        requestItem.setStockTypeId(record.getStockTypeId());
        requestItem.setStockTypeName(record.getStockTypeName());
        requestItem.setMakerId(record.getMakerId());
        requestItem.setMakerName(record.getMakerName());
        requestItem.setWarehouseId(form.getWarehouseId());
        requestItem.setPrice(record.getPrice());
        requestItem.setExchangeRate(BigDecimal.ONE);
        requestItem.setCurrency(record.getCurrency() == null ? CommonConstant.DEFAULT_CURRENCY_JPY : record.getCurrency());
        requestItem.setDiscount(BigDecimal.ONE);
        requestItem.setRequestQty(record.getChangeQty());
        requestItem.setApproveQty(0);
        requestItem.setOutQty(record.getChangeQty());
        requestItem.setTotalAmt(safeAmount(record.getPrice())
                .multiply(BigDecimal.valueOf(record.getChangeQty() == null ? 0 : record.getChangeQty())));
        requestItem.setStockRecordId(record.getId());
        requestItem.setState(StockBizConstant.REQUEST_ITEM_STATE_ADDED);
        requestItem.setRemark(remark);
        return requestItem;
    }
    private void recalculateRequestFormSummary(Long requestId) {
        RequestForm form = this.getByIdNotDeleted(requestId);
        if (form == null) {
            throw new RuntimeException("申請書が見つかりません");
        }
        List<RequestItem> items = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", requestId)
                .eq("state", StockBizConstant.REQUEST_ITEM_STATE_ADDED)
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        int totalQty = 0;
        BigDecimal totalAmt = BigDecimal.ZERO;
        for (RequestItem item : items) {
            totalQty += item.getRequestQty() == null ? 0 : Math.abs(item.getRequestQty());
            totalAmt = totalAmt.add(safeAmount(item.getTotalAmt()));
        }
        form.setTotalQty(totalQty);
        form.setRequestQty(totalQty);
        form.setTotalAmt(totalAmt);
        if (!this.updateById(form)) {
            throw new RuntimeException("申請書集計の更新に失敗しました");
        }
    }

    private List<StockOrder> listAvailableOrdersForRequest(Long userId, Long warehouseId) {
        QueryWrapper<StockOrder> wrapper = new QueryWrapper<StockOrder>()
                .eq("deleted", DeleteEnum.UNDELETED.getCode());
        if (warehouseId != null) {
            wrapper.eq("warehouse_id", warehouseId);
        }
        if (!permissionQueryService.isSuperAdmin(userId)) {
            wrapper.and(w -> w.eq("requester_id", userId).or().eq("operator_id", userId));
        }
        return stockOrderService.list(wrapper);
    }

    private List<StockRecord> listAvailableStockRecordsForRequest(Long userId, Long warehouseId) {
        QueryWrapper<StockRecord> wrapper = new QueryWrapper<StockRecord>()
                .eq("deleted", DeleteEnum.UNDELETED.getCode());
        if (warehouseId != null) {
            wrapper.eq("warehouse_id", warehouseId);
        }
        if (!permissionQueryService.isSuperAdmin(userId)) {
            wrapper.and(w -> w.eq("requester_id", userId).or().eq("operator_id", userId));
        }
        return stockRecordService.list(wrapper);
    }

    private List<StockRecord> listSourceOutboundRecords(RequestForm form) {
        requireOwnedSourceOutbound(form);
        return stockRecordService.list(new QueryWrapper<StockRecord>()
                .eq("order_id", form.getSourceOrderId())
                .eq("order_type", StockBizConstant.ORDER_TYPE_OUTBOUND)
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
    }

    private Set<Long> findAvailableStockRecordIds(Long userId, Long warehouseId) {
        List<StockRecord> records = listAvailableStockRecordsForRequest(userId, warehouseId);
        java.util.HashSet<Long> ids = new java.util.HashSet<>();
        for (StockRecord record : records) {
            ids.add(record.getId());
        }
        return ids;
    }

    private void validateKnifeHandleQuantity(Long requestId) {
        List<RequestItem> items = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", requestId)
                .eq("state", StockBizConstant.REQUEST_ITEM_STATE_ADDED)
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        int knifeQty = 0;
        int handleQty = 0;
        for (RequestItem item : items) {
            int qty = item.getRequestQty() == null ? 0 : item.getRequestQty();
            if (isKnifeItem(item)) {
                knifeQty += qty;
            }
            if (isHandleItem(item)) {
                handleQty += qty;
            }
        }
        if (knifeQty > 0 && handleQty > 0 && knifeQty != handleQty) {
            throw new RuntimeException("刀と柄の数量が一致していません");
        }
    }

    private boolean isKnifeItem(RequestItem item) {
        return containsTypeKeyword(item.getCategoryName(), "\u5200")
                || containsTypeKeyword(item.getGoodsName(), "\u5200")
                || containsTypeKeyword(item.getSeriesName(), "\u5200");
    }

    private boolean isHandleItem(RequestItem item) {
        return containsTypeKeyword(item.getCategoryName(), "\u67C4")
                || containsTypeKeyword(item.getGoodsName(), "\u67C4")
                || containsTypeKeyword(item.getSeriesName(), "\u67C4");
    }

    private boolean containsTypeKeyword(String value, String keyword) {
        return value != null && value.contains(keyword);
    }
private Set<Long> findAvailableOrderItemIds(Long userId, Long warehouseId) {
        List<StockOrder> orders = listAvailableOrdersForRequest(userId, warehouseId);
        java.util.HashSet<Long> ids = new java.util.HashSet<>();
        if (orders.isEmpty()) {
            return ids;
        }
        List<Long> orderIds = new ArrayList<>();
        for (StockOrder order : orders) {
            orderIds.add(order.getId());
        }
        List<StockOrderItem> orderItems = stockOrderItemService.list(new QueryWrapper<StockOrderItem>()
                .in("order_id", orderIds)
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        for (StockOrderItem item : orderItems) {
            ids.add(item.getId());
        }
        return ids;
    }

    private Stock findStock(Long goodsId, Long skuId, Long warehouseId, Long stockTypeId) {
        QueryWrapper<Stock> wrapper = new QueryWrapper<Stock>()
                .eq("goods_id", goodsId)
                .eq("sku_id", skuId)
                .eq("warehouse_id", warehouseId)
                .eq("deleted", DeleteEnum.UNDELETED.getCode());
        if (stockTypeId == null) {
            wrapper.isNull("stock_type_id");
        } else {
            wrapper.eq("stock_type_id", stockTypeId);
        }
        return stockMapper.selectOne(wrapper);
    }

    private String generateRequestNo() {
        return "REQ" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private String generateInboundNo() {
        return "IN" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private void validateCustomerOwnership(Long customerId, Long userId) {
        if (customerId == null || permissionQueryService.isSuperAdmin(userId)) {
            return;
        }
        Customer customer = customerService.getByIdNotDeleted(customerId);
        if (customer == null || !userId.equals(customer.getOwnerUserId())) {
            throw new RuntimeException("自分名義の顧客のみ選択できます");
        }
    }

    private void requireOwned(RequestForm form) {
        if (form == null) {
            return;
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId) && !userId.equals(form.getUserId())) {
            throw new RuntimeException("この申請書データにアクセスする権限がありません");
        }
    }

    @Override
    protected RequestFormVO toVO(RequestForm entity) {
        if (entity == null) {
            return null;
        }
        RequestFormVO vo = new RequestFormVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> RequestForm toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        RequestForm entity = new RequestForm();
        BeanUtils.copyProperties(dto, entity);
        if (entity.getId() == null) {
            entity.setBizNo(generateRequestNo());
        }
        return entity;
    }
}
