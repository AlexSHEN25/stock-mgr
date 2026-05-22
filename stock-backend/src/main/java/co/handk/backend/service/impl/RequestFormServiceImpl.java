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
import co.handk.common.model.dto.update.UpdateRequestFormDTO;
import co.handk.common.model.vo.RequestFormVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RequestFormServiceImpl extends BaseServiceImpl<RequestFormMapper, RequestForm, RequestFormVO>
        implements RequestFormService {

    @Autowired private StockOrderService stockOrderService;
    @Autowired private StockOrderItemService stockOrderItemService;
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
        form.setState(StockBizConstant.REQUEST_STATE_CREATED);
        form.setApproveRemark(dto.getRemark());

        int totalQty = 0;
        BigDecimal totalAmt = BigDecimal.ZERO;

        if (!this.save(form)) {
            throw new RuntimeException("申請書の保存に失敗しました");
        }

        for (StockOrderItem orderItem : selectedItems) {
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
            requestItem.setWarehouseId(outboundOrder.getWarehouseId());
            requestItem.setPrice(orderItem.getPrice());
            requestItem.setExchangeRate(BigDecimal.ONE);
            requestItem.setCurrency(orderItem.getCurrency() == null ? CommonConstant.DEFAULT_CURRENCY_JPY : orderItem.getCurrency());
            requestItem.setDiscount(BigDecimal.ONE);
            requestItem.setRequestQty(orderItem.getChangeQty());
            requestItem.setApproveQty(0);
            requestItem.setOutQty(orderItem.getChangeQty());
            requestItem.setTotalAmt(safeAmount(orderItem.getPrice()).multiply(BigDecimal.valueOf(orderItem.getChangeQty() == null ? 0 : orderItem.getChangeQty())));
            requestItem.setStockRecordId(null);
            requestItem.setRemark(dto.getRemark());
            if (!requestItemService.save(requestItem)) {
                throw new RuntimeException("申請明細の保存に失敗しました");
            }

            totalQty += orderItem.getChangeQty() == null ? 0 : orderItem.getChangeQty();
            totalAmt = totalAmt.add(safeAmount(requestItem.getTotalAmt()));
        }

        form.setTotalQty(totalQty);
        form.setRequestQty(totalQty);
        form.setTotalAmt(totalAmt);
        if (!this.updateById(form)) {
            throw new RuntimeException("申請書の集計更新に失敗しました");
        }
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

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
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