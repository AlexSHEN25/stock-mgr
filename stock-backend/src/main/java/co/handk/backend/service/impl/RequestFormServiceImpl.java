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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
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
import java.nio.file.Files;
import java.nio.file.Path;
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
    private static final String TEMPLATE_DEFAULT = "template/request_form_template.xlsx";
    private static final String TEMPLATE_A = "template/request_form_template_A.xlsx";
    private static final String TEMPLATE_B = "template/request_form_template_B.xlsx";
    private static final String TEMPLATE_C = "template/request_form_template_C.xlsx";
    private static final String FORMAT_PDF = "pdf";

    @Autowired private StockOrderService stockOrderService;
    @Autowired private StockOrderItemService stockOrderItemService;
    @Autowired private StockRecordService stockRecordService;
    @Autowired private RequestItemService requestItemService;
    @Autowired private UserService userService;
    @Autowired private DeptService deptService;
    @Autowired private StockMapper stockMapper;
    @Autowired private PermissionQueryService permissionQueryService;
    @Autowired private CustomerService customerService;
    @Autowired private ConfigService configService;

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
            throw new RuntimeException("request form operation failed");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(outboundOrder.getOrderType())) {
            throw new RuntimeException("request form operation failed");
        }

        Long loginUserId = UserContext.getUserIdOrDefault();
        if (!loginUserId.equals(outboundOrder.getRequesterId()) && !loginUserId.equals(outboundOrder.getOperatorId())) {
            throw new RuntimeException("request form operation failed");
        }

        List<StockOrderItem> allItems = stockOrderItemService.list(new QueryWrapper<StockOrderItem>()
                .eq("order_id", outboundOrder.getId())
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        if (allItems.isEmpty()) {
            throw new RuntimeException("request form operation failed");
        }

        List<StockOrderItem> selectedItems = filterItems(allItems, dto.getStockOrderItemIds());
        if (selectedItems.isEmpty()) {
            throw new RuntimeException("request form operation failed");
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
            throw new RuntimeException("request form operation failed");
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
            int requestQty = stockRecord.getChangeQty() == null ? 0 : Math.abs(stockRecord.getChangeQty());
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
            throw new RuntimeException("request form operation failed");
        }
        validateSourceBalance(form);
        return form.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reapplyInbound(Long requestId) {
        RequestForm form = this.getByIdNotDeleted(requestId);
        if (form == null) {
            throw new RuntimeException("request form operation failed");
        }

        List<RequestItem> items = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", requestId)
                .eq("state", StockBizConstant.REQUEST_ITEM_STATE_ADDED)
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        if (items.isEmpty()) {
            throw new RuntimeException("request form operation failed");
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
        inboundOrder.setRemark("鬯ｯ・ｯ繝ｻ・ｨ郢晢ｽｻ繝ｻ・ｾ鬮ｯ蜈ｷ・ｽ・ｹ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｳ鬯ｯ・ｯ繝ｻ・ｮ郢晢ｽｻ繝ｻ・ｫ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｲ鬯ｮ・ｯ陷茨ｽｷ繝ｻ・ｽ繝ｻ・ｹ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｺ鬯ｮ・ｯ雋・ｽｷ髫ｱ・ｿ鬯ｮ・ｴ隲帙・・ｽ・ｫ繝ｻ・､髯ｷ・ｻ繝ｻ・ｵ郢晢ｽｻ繝ｻ・ｮ髯橸ｽｳ髣鯉ｽｨ繝ｻ・ｽ繝ｻ・ｿ郢晢ｽｻ繝ｻ・ｫ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬯ｯ・ｮ繝ｻ・ｯ髮九・・ｽ・ｯ郢晢ｽｻ繝ｻ・ｶ郢晢ｽｻ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｫ: " + form.getBizNo());
        inboundOrder.setBizDate(LocalDate.now());

        int totalQty = 0;
        for (RequestItem item : items) {
            totalQty += item.getRequestQty() == null ? 0 : item.getRequestQty();
        }
        inboundOrder.setTotalQty(totalQty);
        inboundOrder.setStockTypeId(items.get(0).getStockTypeId());
        if (!stockOrderService.save(inboundOrder)) {
            throw new RuntimeException("request form operation failed");
        }

        for (RequestItem reqItem : items) {
            Stock stock = findStock(reqItem.getGoodsId(), reqItem.getSkuId(), reqItem.getWarehouseId(), reqItem.getStockTypeId());
            if (stock == null) {
                throw new RuntimeException("request form operation failed");
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
            orderItem.setRemark("鬯ｯ・ｯ繝ｻ・ｨ郢晢ｽｻ繝ｻ・ｾ鬮ｯ蜈ｷ・ｽ・ｹ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｳ鬯ｯ・ｯ繝ｻ・ｮ郢晢ｽｻ繝ｻ・ｫ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｲ鬯ｮ・ｯ陷茨ｽｷ繝ｻ・ｽ繝ｻ・ｹ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｺ鬯ｮ・ｯ雋・ｽｷ髫ｱ・ｿ鬯ｮ・ｴ隲帙・・ｽ・ｫ繝ｻ・､髯ｷ・ｻ繝ｻ・ｵ郢晢ｽｻ繝ｻ・ｮ髯橸ｽｳ髣鯉ｽｨ繝ｻ・ｽ繝ｻ・ｿ郢晢ｽｻ繝ｻ・ｫ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬯ｯ・ｮ繝ｻ・ｯ髮九・・ｽ・ｯ郢晢ｽｻ繝ｻ・ｶ郢晢ｽｻ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｫ鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮｣蛹・ｽｽ・ｳ髫ｶ蜴・ｽｽ・ｸ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｮ鬯ｮ・｣鬲・ｼ夲ｽｽ・ｽ繝ｻ・ｨ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｴ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｰ");
            orderItem.setBizDate(LocalDate.now());
            if (!stockOrderItemService.save(orderItem)) {
                throw new RuntimeException("request form operation failed");
            }
        }

        form.setState(StockBizConstant.REQUEST_STATE_REINBOUND_APPLIED);
        if (!this.updateById(form)) {
            throw new RuntimeException("request form operation failed");
        }
        return inboundOrder.getId();
    }

        @Override
    public List<RequestCandidateItemVO> listCandidateItems(Long requestId) {
        RequestForm form = this.getByIdNotDeleted(requestId);
        if (form == null) {
            throw new RuntimeException("request form operation failed");
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

            int currentQty = existing == null || !Integer.valueOf(StockBizConstant.REQUEST_ITEM_STATE_ADDED).equals(existing.getState())
                    || existing.getRequestQty() == null ? 0 : Math.abs(existing.getRequestQty());
            applyOutboundRemainderByRequested(stockRecord, requestedQty, currentQty);

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
    public void downloadRequestForm(Long requestId, String format, HttpServletResponse response) {
        if (FORMAT_PDF.equalsIgnoreCase(format)) {
            downloadRequestFormPdf(requestId, response);
            return;
        }
        downloadBDeptRequestForm(requestId, response);
    }

    @Override
    public void downloadBDeptRequestForm(Long requestId, HttpServletResponse response) {
        RequestForm form = this.getByIdNotDeleted(requestId);
        if (form == null) {
            throw new RuntimeException("request form operation failed");
        }

        try (XSSFWorkbook workbook = buildRequestWorkbook(form)) {
            String filename = "request_" + form.getBizNo() + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            workbook.write(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException("request form operation failed");
        }
    }

    private void downloadRequestFormPdf(Long requestId, HttpServletResponse response) {
        RequestForm form = this.getByIdNotDeleted(requestId);
        if (form == null) {
            throw new RuntimeException("request form operation failed");
        }
        List<RequestItem> items = listRequestItems(requestId);
        Customer customer = form.getCustomerId() == null ? null : customerService.getByIdNotDeleted(form.getCustomerId());

        try (PDDocument document = new PDDocument()) {
            writeRequestPdf(document, form, customer, items);
            String filename = "request_" + form.getBizNo() + ".pdf";
            response.setContentType("application/pdf");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            document.save(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException("request form pdf generation failed");
        }
    }

    private XSSFWorkbook buildRequestWorkbook(RequestForm form) throws IOException {
        List<RequestItem> items = listRequestItems(form.getId());
        Customer customer = form.getCustomerId() == null ? null : customerService.getByIdNotDeleted(form.getCustomerId());
        InputStream templateInput = openTemplateInputStream(form);
        XSSFWorkbook workbook = new XSSFWorkbook(templateInput);
        templateInput.close();
        Sheet sheet = workbook.getSheetAt(0);
        workbook.setSheetName(0, safeSheetName(form.getBizNo()));
        while (workbook.getNumberOfSheets() > 1) {
            workbook.removeSheetAt(workbook.getNumberOfSheets() - 1);
        }
        fillHeader(sheet, customer);
        fillItems(sheet, items);
        return workbook;
    }

    private List<RequestItem> listRequestItems(Long requestId) {
        List<RequestItem> items = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", requestId)
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("request form operation failed");
        }
        return items;
    }

    private void writeRequestPdf(PDDocument document, RequestForm form, Customer customer, List<RequestItem> items)
            throws IOException {
        PdfPageWriter writer = new PdfPageWriter(document);
        writer.addPage();
        writer.text("REQUEST FORM", 18, true);
        writer.text("Request No: " + safe(form.getBizNo()), 10, false);
        writer.text("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-M-d")), 10, false);
        if (customer != null) {
            writer.text("MESSRS: " + safe(customer.getName()), 10, false);
            writer.text("Address: " + safe(customer.getAddress()), 10, false);
            writer.text("Tel: " + safe(customer.getPhone()) + "    EMAIL: " + safe(customer.getEmail()), 10, false);
        }
        writer.text("Total Qty: " + (form.getRequestQty() == null ? 0 : form.getRequestQty())
                + "    Total Amount: " + formatCurrency(CommonConstant.DEFAULT_CURRENCY_JPY, form.getTotalAmt()), 10, false);
        writer.blank(8);
        writer.text("No.  Brand  Item  Qty  Unit Price  Amount  SKU  Remark", 9, true);
        writer.line();
        int index = 1;
        for (RequestItem item : items) {
            writer.text(index + ".  "
                    + truncate(safe(item.getBrandName()), 12) + "  "
                    + truncate(safe(item.getGoodsName()), 24) + "  "
                    + (item.getRequestQty() == null ? 0 : item.getRequestQty()) + "  "
                    + formatCurrency(item.getCurrency(), item.getPrice()) + "  "
                    + formatCurrency(item.getCurrency(), item.getTotalAmt()) + "  "
                    + truncate(safe(item.getSkuCode()), 16) + "  "
                    + truncate(safe(item.getRemark()), 18), 8, false);
            index++;
        }
        writer.close();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private String sanitizePdfText(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            sb.append(ch >= 32 && ch <= 126 ? ch : '?');
        }
        return sb.toString();
    }

    private final class PdfPageWriter {
        private static final float MARGIN = 42F;
        private static final float BOTTOM_MARGIN = 42F;
        private final PDDocument document;
        private final PDFont regularFont = PDType1Font.HELVETICA;
        private final PDFont boldFont = PDType1Font.HELVETICA_BOLD;
        private PDPageContentStream content;
        private float y;

        private PdfPageWriter(PDDocument document) {
            this.document = document;
        }

        private void addPage() throws IOException {
            close();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            content = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - MARGIN;
        }

        private void text(String value, int fontSize, boolean bold) throws IOException {
            ensureSpace(fontSize + 8);
            content.beginText();
            content.setFont(bold ? boldFont : regularFont, fontSize);
            content.newLineAtOffset(MARGIN, y);
            content.showText(sanitizePdfText(value));
            content.endText();
            y -= fontSize + 7F;
        }

        private void line() throws IOException {
            ensureSpace(8);
            content.moveTo(MARGIN, y);
            content.lineTo(PDRectangle.A4.getWidth() - MARGIN, y);
            content.stroke();
            y -= 8F;
        }

        private void blank(float height) throws IOException {
            ensureSpace(height);
            y -= height;
        }

        private void ensureSpace(float height) throws IOException {
            if (content == null || y - height < BOTTOM_MARGIN) {
                addPage();
            }
        }

        private void close() throws IOException {
            if (content != null) {
                content.close();
                content = null;
            }
        }
    }

    private InputStream openTemplateInputStream(RequestForm form) throws IOException {
        String templatePath = resolveTemplatePath(form);
        Path filePath = Path.of(templatePath);
        if (Files.isRegularFile(filePath)) {
            return Files.newInputStream(filePath);
        }
        ClassPathResource classPathResource = new ClassPathResource(templatePath);
        if (classPathResource.exists()) {
            return classPathResource.getInputStream();
        }
        ClassPathResource fallback = new ClassPathResource(TEMPLATE_DEFAULT);
        if (fallback.exists()) {
            return fallback.getInputStream();
        }
        throw new IOException("template not found: classpath:" + templatePath);
    }

    private String resolveTemplatePath(RequestForm form) {
        String configuredTemplate = resolveTemplatePathFromConfig(form);
        if (configuredTemplate != null && !configuredTemplate.isBlank()) {
            return configuredTemplate;
        }
        String deptName = form == null || form.getDeptName() == null ? "" : form.getDeptName().toUpperCase();
        if (deptName.contains("A")) {
            return TEMPLATE_A;
        }
        if (deptName.contains("B")) {
            return TEMPLATE_B;
        }
        if (deptName.contains("C")) {
            return TEMPLATE_C;
        }
        Long deptId = form == null ? null : form.getDeptId();
        if (deptId != null) {
            if (deptId == 1L) {
                return TEMPLATE_A;
            }
            if (deptId == 2L) {
                return TEMPLATE_B;
            }
            if (deptId == 3L) {
                return TEMPLATE_C;
            }
        }
        return TEMPLATE_A;
    }

    private String resolveTemplatePathFromConfig(RequestForm form) {
        String configKey = resolveTemplateConfigKey(form);
        String value = getConfigValueByName(configKey);
        if (value == null || value.isBlank()) {
            value = getConfigValueByName("request.form.template.default");
        }
        return normalizeTemplatePath(value);
    }

    private String resolveTemplateConfigKey(RequestForm form) {
        String deptName = form == null || form.getDeptName() == null ? "" : form.getDeptName().toUpperCase();
        if (deptName.contains("A")) {
            return "request.form.template.A";
        }
        if (deptName.contains("B")) {
            return "request.form.template.B";
        }
        if (deptName.contains("C")) {
            return "request.form.template.C";
        }
        Long deptId = form == null ? null : form.getDeptId();
        if (deptId != null) {
            if (deptId == 1L) {
                return "request.form.template.A";
            }
            if (deptId == 2L) {
                return "request.form.template.B";
            }
            if (deptId == 3L) {
                return "request.form.template.C";
            }
        }
        return "request.form.template.A";
    }

    private String getConfigValueByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        Config config = configService.getOne(new QueryWrapper<Config>()
                .eq("name", name)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
        return config == null ? null : config.getValue();
    }

    private String normalizeTemplatePath(String path) {
        if (path == null) {
            return null;
        }
        String normalized = path.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.startsWith("classpath:")) {
            normalized = normalized.substring("classpath:".length());
        }
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
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
                if (qty < 0) {
                    throw new RuntimeException("requested qty must be >= 0");
                }
                if (qty > 0) {
                    quantities.merge(item.getStockRecordId(), qty, Integer::sum);
                }
            }
            return quantities;
        }
        for (Long stockRecordId : normalizeStockRecordIds(dto.getStockOrderItemIds())) {
            StockRecord record = stockRecordService.getByIdNotDeleted(stockRecordId);
            if (record != null && record.getChangeQty() != null) {
                int qty = Math.abs(record.getChangeQty());
                if (qty > 0) {
                    quantities.put(stockRecordId, qty);
                }
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
        Integer currentRaw = orderItem.getChangeQty();
        int currentQty = currentRaw == null ? 0 : Math.abs(currentRaw);
        int nextQty = currentQty - deltaQty;
        if (nextQty < 0 || nextQty > originalQty) {
            throw new RuntimeException("requested qty cannot exceed available outbound qty");
        }
        int affected = stockOrderItemService.getBaseMapper().update(
                null,
                new LambdaUpdateWrapper<StockOrderItem>()
                        .eq(StockOrderItem::getId, orderItem.getId())
                        .eq(StockOrderItem::getDeleted, DeleteEnum.UNDELETED.getCode())
                        .eq(StockOrderItem::getChangeQty, currentRaw)
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

    private void applyOutboundRemainderByRequested(StockRecord record, int requestedQty, int currentRequestQty) {
        StockOrderItem orderItem = stockOrderItemService.getByIdNotDeleted(record.getOrderItemId());
        if (orderItem == null) {
            throw new RuntimeException("source outbound item not found");
        }
        int originalQty = record.getChangeQty() == null ? 0 : Math.abs(record.getChangeQty());
        Integer currentRaw = orderItem.getChangeQty();
        int currentRemainingQty = currentRaw == null ? 0 : Math.abs(currentRaw);

        int maxRequestQty = currentRemainingQty + Math.max(0, currentRequestQty);
        if (maxRequestQty > originalQty) {
            maxRequestQty = originalQty;
        }
        if (requestedQty < 0 || requestedQty > maxRequestQty) {
            throw new RuntimeException("requested qty cannot exceed available outbound qty"
                    + ", stockRecordId=" + record.getId()
                    + ", available=" + maxRequestQty
                    + ", requested=" + requestedQty);
        }

        int targetRemainingQty = currentRemainingQty + Math.max(0, currentRequestQty) - requestedQty;
        if (targetRemainingQty < 0 || targetRemainingQty > originalQty) {
            throw new RuntimeException("requested qty cannot exceed available outbound qty"
                    + ", stockRecordId=" + record.getId()
                    + ", available=" + maxRequestQty
                    + ", requested=" + requestedQty
                    + ", targetRemaining=" + targetRemainingQty
                    + ", original=" + originalQty);
        }

        int affected = stockOrderItemService.getBaseMapper().update(
                null,
                new LambdaUpdateWrapper<StockOrderItem>()
                        .eq(StockOrderItem::getId, orderItem.getId())
                        .eq(StockOrderItem::getDeleted, DeleteEnum.UNDELETED.getCode())
                        .eq(StockOrderItem::getChangeQty, currentRaw)
                        .set(StockOrderItem::getChangeQty, targetRemainingQty)
        );
        if (affected <= 0) {
            throw new RuntimeException("source outbound item changed concurrently, please retry");
        }

        StockOrder order = stockOrderService.getByIdNotDeleted(record.getOrderId());
        if (order != null) {
            int totalQty = order.getTotalQty() == null ? 0 : order.getTotalQty();
            int delta = targetRemainingQty - currentRemainingQty;
            int orderAffected = stockOrderService.getBaseMapper().update(
                    null,
                    new LambdaUpdateWrapper<StockOrder>()
                            .eq(StockOrder::getId, order.getId())
                            .eq(StockOrder::getDeleted, DeleteEnum.UNDELETED.getCode())
                            .eq(StockOrder::getTotalQty, totalQty)
                            .set(StockOrder::getTotalQty, Math.max(0, totalQty + delta))
            );
            if (orderAffected <= 0) {
                throw new RuntimeException("source outbound order changed concurrently, please retry");
            }
        }
    }
private void validateSourceBalance(RequestForm form) {
        List<StockRecord> records = listSourceOutboundRecords(form);
        for (StockRecord record : records) {
            int originalQty = record.getChangeQty() == null ? 0 : Math.abs(record.getChangeQty());
            StockOrderItem orderItem = stockOrderItemService.getByIdNotDeleted(record.getOrderItemId());
            int remainingQty = orderItem == null || orderItem.getChangeQty() == null ? 0 : Math.abs(orderItem.getChangeQty());
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
        requestItem.setRequestQty(orderItem.getChangeQty() == null ? 0 : Math.abs(orderItem.getChangeQty()));
        requestItem.setApproveQty(0);
        requestItem.setOutQty(orderItem.getChangeQty() == null ? 0 : Math.abs(orderItem.getChangeQty()));
        requestItem.setTotalAmt(safeAmount(orderItem.getPrice())
                .multiply(BigDecimal.valueOf(orderItem.getChangeQty() == null ? 0 : Math.abs(orderItem.getChangeQty()))));
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
        requestItem.setRequestQty(record.getChangeQty() == null ? 0 : Math.abs(record.getChangeQty()));
        requestItem.setApproveQty(0);
        requestItem.setOutQty(record.getChangeQty() == null ? 0 : Math.abs(record.getChangeQty()));
        requestItem.setTotalAmt(safeAmount(record.getPrice())
                .multiply(BigDecimal.valueOf(record.getChangeQty() == null ? 0 : Math.abs(record.getChangeQty()))));
        requestItem.setStockRecordId(record.getId());
        requestItem.setState(StockBizConstant.REQUEST_ITEM_STATE_ADDED);
        requestItem.setRemark(remark);
        return requestItem;
    }
    private void recalculateRequestFormSummary(Long requestId) {
        RequestForm form = this.getByIdNotDeleted(requestId);
        if (form == null) {
            throw new RuntimeException("request form operation failed");
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
            throw new RuntimeException("request form operation failed");
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
            throw new RuntimeException("request form operation failed");
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
            throw new RuntimeException("request form operation failed");
        }
    }

    private void requireOwned(RequestForm form) {
        if (form == null) {
            return;
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId) && !userId.equals(form.getUserId())) {
            throw new RuntimeException("request form operation failed");
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
