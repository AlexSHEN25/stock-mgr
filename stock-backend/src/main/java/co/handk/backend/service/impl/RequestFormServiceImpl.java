package co.handk.backend.service.impl;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.entity.*;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.mapper.RequestFormMapper;
import co.handk.backend.mapper.StockMapper;
import co.handk.backend.service.*;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.StockBizConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateRequestFromOutboundDTO;
import co.handk.common.model.dto.create.CreateRequestFormDTO;
import co.handk.common.model.dto.query.RequestItemCartQueryDTO;
import co.handk.common.model.dto.update.RequestCartMoveDTO;
import co.handk.common.model.dto.update.RequestFormItemBatchDTO;
import co.handk.common.model.dto.update.RequestFormWithItemsDTO;
import co.handk.common.model.dto.update.UpdateRequestFormDTO;
import co.handk.common.model.vo.RequestCandidateItemVO;
import co.handk.common.model.vo.RequestFormCartPreviewVO;
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
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RequestFormServiceImpl extends BaseServiceImpl<RequestFormMapper, RequestForm, RequestFormVO>
        implements RequestFormService {
    private static final String TEMPLATE_CODE_A = "A";
    private static final String TEMPLATE_CODE_B = "B";
    private static final String TEMPLATE_CODE_C = "C";
    private static final String TEMPLATE_DEFAULT = "template/request_form_template.xlsx";
    private static final String TEMPLATE_A = "template/request_form_template_A.xlsx";
    private static final String TEMPLATE_B = "template/request_form_template_B.xlsx";
    private static final String TEMPLATE_C = "template/request_form_template_C.xlsx";
    private static final String CONFIG_TEMPLATE_DEFAULT = "request.form.template.default";
    private static final String CONFIG_KNIFE_CATEGORY_KEYWORDS = "request.item.category.knife.keywords";
    private static final String CONFIG_HANDLE_CATEGORY_KEYWORDS = "request.item.category.handle.keywords";
    private static final String FORMAT_PDF = "pdf";
    private static final String REMARK_REAPPLY_INBOUND = "Reapply inbound for request form: ";
    private static final String REMARK_REAPPLY_INBOUND_ITEM = "Reapply inbound from request form";
    private static final String REMARK_DELIVERY_INBOUND = "Delivery schedule inbound from stock record: ";
    private static final String DEFAULT_ERROR_CODE = "RF000";
    private static final Map<String, String> ERROR_CODE_BY_MESSAGE = Map.ofEntries(
            Map.entry("request form operation failed", DEFAULT_ERROR_CODE),
            Map.entry("request form not found", "RF001"),
            Map.entry("request form source outbound order is required", "RF002"),
            Map.entry("source outbound order not found", "RF003"),
            Map.entry("source outbound order is not owned by current user", "RF004"),
            Map.entry("selected stock record is not available", "RF005"),
            Map.entry("source outbound item not found", "RF006"),
            Map.entry("requested qty must be >= 0", "RF007"),
            Map.entry("requested qty cannot exceed available outbound qty", "RF008"),
            Map.entry("source outbound item changed concurrently, please retry", "RF009"),
            Map.entry("source outbound order changed concurrently, please retry", "RF010"),
            Map.entry("source outbound stock record not found", "RF011"),
            Map.entry("failed to save request item", "RF012"),
            Map.entry("failed to add request item", "RF013"),
            Map.entry("failed to update request item", "RF014"),
            Map.entry("failed to remove request item", "RF015"),
            Map.entry("request and outbound quantities are inconsistent", "RF016"),
            Map.entry("request form pdf generation failed", "RF017"),
            Map.entry("request form workbook generation failed", "RF018"),
            Map.entry("customer is not owned by current user", "RF019"),
            Map.entry("knife and handle quantities must match", "RF020"),
            Map.entry("source outbound order has no items", "RF021"),
            Map.entry("no source outbound items selected", "RF022"),
            Map.entry("failed to save request form", "RF023"),
            Map.entry("failed to update request form", "RF024"),
            Map.entry("request form has no active items", "RF025"),
            Map.entry("failed to create inbound order", "RF026"),
            Map.entry("stock not found for request item", "RF027"),
            Map.entry("failed to save inbound order item", "RF028"),
            Map.entry("request form is not owned by current user", "RF029"),
            Map.entry("source outbound order is not finished", "RF030"),
            Map.entry("request form already reapplied inbound", "RF031"),
            Map.entry("request form is completed and cannot be modified", "RF032"),
            Map.entry("request form state is not allowed for current user", "RF033"),
            Map.entry("request form state is not editable for current user", "RF034")
    );
    private static final Map<String, String> ERROR_MESSAGE_JA_BY_MESSAGE = Map.ofEntries(
            Map.entry("request form operation failed", "請求書処理に失敗しました"),
            Map.entry("request form not found", "請求書が見つかりません"),
            Map.entry("request form source outbound order is required", "元出庫伝票は必須です"),
            Map.entry("source outbound order not found", "元出庫伝票が見つかりません"),
            Map.entry("source outbound order is not owned by current user", "現在のユーザーは元出庫伝票を操作できません"),
            Map.entry("selected stock record is not available", "選択した在庫履歴は利用できません"),
            Map.entry("source outbound item not found", "元出庫明細が見つかりません"),
            Map.entry("requested qty must be >= 0", "請求数量は0以上で入力してください"),
            Map.entry("requested qty cannot exceed available outbound qty", "請求数量は出庫可能数量を超えられません"),
            Map.entry("source outbound item changed concurrently, please retry", "元出庫明細が更新されました。再試行してください"),
            Map.entry("source outbound order changed concurrently, please retry", "元出庫伝票が更新されました。再試行してください"),
            Map.entry("source outbound stock record not found", "元出庫在庫履歴が見つかりません"),
            Map.entry("failed to save request item", "請求明細の保存に失敗しました"),
            Map.entry("failed to add request item", "請求明細の追加に失敗しました"),
            Map.entry("failed to update request item", "請求明細の更新に失敗しました"),
            Map.entry("failed to remove request item", "請求明細の削除に失敗しました"),
            Map.entry("request and outbound quantities are inconsistent", "請求数量と出庫数量が一致しません"),
            Map.entry("request form pdf generation failed", "請求書PDFの生成に失敗しました"),
            Map.entry("request form workbook generation failed", "請求書Excelの生成に失敗しました"),
            Map.entry("customer is not owned by current user", "現在のユーザーはこの顧客を操作できません"),
            Map.entry("knife and handle quantities must match", "刀身と柄の数量は一致する必要があります"),
            Map.entry("source outbound order has no items", "元出庫伝票に明細がありません"),
            Map.entry("no source outbound items selected", "元出庫明細を選択してください"),
            Map.entry("failed to save request form", "請求書の保存に失敗しました"),
            Map.entry("failed to update request form", "請求書の更新に失敗しました"),
            Map.entry("request form has no active items", "有効な請求明細がありません"),
            Map.entry("failed to create inbound order", "入庫伝票の作成に失敗しました"),
            Map.entry("stock not found for request item", "請求明細の在庫が見つかりません"),
            Map.entry("failed to save inbound order item", "入庫明細の保存に失敗しました"),
            Map.entry("request form is not owned by current user", "現在のユーザーはこの請求書を操作できません"),
            Map.entry("source outbound order is not finished", "元出庫伝票は完了していません"),
            Map.entry("request form already reapplied inbound", "請求書はすでに再入庫済みです"),
            Map.entry("request form is completed and cannot be modified", "完了済みの請求書は変更できません"),
            Map.entry("request form state is not allowed for current user", "現在のユーザーはこの請求書状態を設定できません"),
            Map.entry("request form state is not editable for current user", "現在のユーザーはこの請求書状態を編集できません")
    );

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
            applyCurrentUser(createDto, userId);
            applyRequestFormCreateDefaults(createDto);
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
            requireRequestFormEditable(existed);
            Long userId = UserContext.getUserIdOrDefault();
            validateRequestFormState(updateDto.getState(), userId);
            preserveRequestFormBackendFields(updateDto, existed);
            applyCustomerName(updateDto);
            validateCustomerOwnership(updateDto.getCustomerId(), userId);
        }
        return super.updateByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveWithItems(RequestFormWithItemsDTO dto) {
        prepareCartBundleDefaults(dto);
        RequestForm existing = findExistingForm(dto);
        Long requestId;
        if (existing == null) {
            requestId = createFormForBundle(dto);
        } else {
            dto.setId(existing.getId());
            updateFormForBundle(dto, existing);
            requestId = existing.getId();
        }
        syncRequestItems(requestId, dto.getItems());
        validateKnifeHandleQuantity(requestId);
        recalculateRequestFormSummary(requestId);
        return requestId;
    }

    private void prepareCartBundleDefaults(RequestFormWithItemsDTO dto) {
        if (dto == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            return;
        }
        Long expectedCustomerId = dto.getCustomerId();
        Long warehouseId = dto.getWarehouseId();
        for (RequestFormWithItemsDTO.Item item : dto.getItems()) {
            for (Long stockRecordId : stockRecordIdsOf(item)) {
                StockRecord record = requireAvailableOutboundRecord(stockRecordId);
                if (record.getCustomerId() == null) {
                    throw fail("customer is required");
                }
                if (expectedCustomerId == null) {
                    expectedCustomerId = record.getCustomerId();
                } else if (!expectedCustomerId.equals(record.getCustomerId())) {
                    throw fail("selected stock records must belong to the same customer");
                }
                if (warehouseId == null) {
                    warehouseId = record.getWarehouseId();
                }
            }
        }
        if (expectedCustomerId != null) {
            dto.setCustomerId(expectedCustomerId);
            Customer customer = customerService.getByIdNotDeleted(expectedCustomerId);
            dto.setCustomerName(customer == null ? dto.getCustomerName() : customer.getName());
        }
        if (warehouseId != null) {
            dto.setWarehouseId(warehouseId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateWithItems(RequestFormWithItemsDTO dto) {
        if (dto.getId() == null) {
            throw fail("request form not found");
        }
        RequestForm existing = super.getByIdNotDeleted(dto.getId());
        if (existing == null) {
            throw fail("request form not found");
        }
        requireRequestFormEditable(existing);
        updateFormForBundle(dto, existing);
        syncRequestItems(existing.getId(), dto.getItems());
        validateKnifeHandleQuantity(existing.getId());
        recalculateRequestFormSummary(existing.getId());
        return true;
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
        if (form == null) {
            return DeleteEnum.UNDELETED.getCode();
        }
        requireOwned(form);
        requireRequestFormEditable(form);
        deleteItemsByRequestId(id);
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
            int rows = 0;
            for (Long id : ids) {
                rows += deleteByIdLogic(id);
            }
            return rows;
        }
        int rows = 0;
        for (Long id : ids) {
            RequestForm form = super.getByIdNotDeleted(id);
            requireOwned(form);
            requireRequestFormEditable(form);
            rows += deleteByIdLogic(id);
        }
        return rows;
    }

    private RequestForm findExistingForm(RequestFormWithItemsDTO dto) {
        if (dto.getId() != null) {
            RequestForm existing = super.getByIdNotDeleted(dto.getId());
            if (existing != null) {
                requireOwned(existing);
            }
            return existing;
        }
        if (dto.getBizNo() == null || dto.getBizNo().isBlank()) {
            return null;
        }
        RequestForm existing = getOne(new QueryWrapper<RequestForm>()
                .eq("biz_no", dto.getBizNo().trim())
                .last("LIMIT 1"));
        if (existing != null) {
            requireOwned(existing);
        }
        return existing;
    }

    private Long createFormForBundle(RequestFormWithItemsDTO dto) {
        CreateRequestFormDTO createDto = new CreateRequestFormDTO();
        BeanUtils.copyProperties(dto, createDto);
        Long userId = UserContext.getUserIdOrDefault();
        applyCurrentUser(createDto, userId);
        applyRequestFormCreateDefaults(createDto);
        validateCustomerOwnership(createDto.getCustomerId(), userId);

        RequestForm form = toEntity(createDto);
        if (dto.getBizNo() != null && !dto.getBizNo().isBlank()) {
            form.setBizNo(dto.getBizNo().trim());
        }
        if (!save(form)) {
            throw fail("failed to save request form");
        }
        return form.getId();
    }

    private void updateFormForBundle(RequestFormWithItemsDTO dto, RequestForm existing) {
        requireOwned(existing);
        requireRequestFormEditable(existing);
        UpdateRequestFormDTO updateDto = new UpdateRequestFormDTO();
        BeanUtils.copyProperties(dto, updateDto);
        updateDto.setId(existing.getId());
        if (updateDto.getCustomerId() == null) {
            updateDto.setCustomerId(existing.getCustomerId());
            updateDto.setCustomerName(existing.getCustomerName());
        }
        if (!updateByDto(updateDto)) {
            throw fail("failed to update request form");
        }
    }

    private void syncRequestItems(Long requestId, List<RequestFormWithItemsDTO.Item> submittedItems) {
        List<RequestFormWithItemsDTO.Item> items = expandAggregatedCartItems(submittedItems);
        List<RequestItem> existingItems = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", requestId));
        Map<Long, RequestItem> existingById = new HashMap<>();
        Map<Long, RequestItem> existingByStockRecordId = new HashMap<>();
        for (RequestItem item : existingItems) {
            existingById.put(item.getId(), item);
            if (item.getStockRecordId() != null) {
                existingByStockRecordId.put(item.getStockRecordId(), item);
            }
        }

        Set<Long> retainedIds = new HashSet<>();
        for (RequestFormWithItemsDTO.Item item : items) {
            item.setRequestId(requestId);
            if (item.getState() == null) {
                item.setState(StockBizConstant.REQUEST_ITEM_STATE_ADDED);
            }
            if (item.getId() == null && item.getStockRecordId() != null) {
                RequestItem matched = existingByStockRecordId.get(item.getStockRecordId());
                if (matched != null) {
                    item.setId(matched.getId());
                }
            }
            if (item.getId() != null) {
                if (!existingById.containsKey(item.getId())) {
                    throw fail("request item is not owned by current request form");
                }
                if (!retainedIds.add(item.getId())) {
                    throw fail("duplicated request item");
                }
            }
        }

        for (RequestItem existing : existingItems) {
            if (!retainedIds.contains(existing.getId())) {
                requestItemService.deleteByIdLogic(existing.getId());
            }
        }

        RequestForm form = super.getByIdNotDeleted(requestId);
        for (RequestFormWithItemsDTO.Item item : items) {
            RequestItem existing = item.getId() == null ? null : existingById.get(item.getId());
            prepareBundleItemFromStockRecord(form, item, existing);
            boolean success;
            if (item.getId() == null) {
                success = requestItemService.saveByDto(item);
            } else {
                success = requestItemService.updateByDto(item);
            }
            if (!success) {
                throw fail(item.getId() == null ? "failed to save request item" : "failed to update request item");
            }
        }
    }

    private List<RequestFormWithItemsDTO.Item> expandAggregatedCartItems(List<RequestFormWithItemsDTO.Item> submittedItems) {
        if (submittedItems == null || submittedItems.isEmpty()) {
            return new ArrayList<>();
        }
        List<RequestFormWithItemsDTO.Item> expanded = new ArrayList<>();
        for (RequestFormWithItemsDTO.Item item : submittedItems) {
            List<Long> stockRecordIds = stockRecordIdsOf(item);
            if (stockRecordIds.size() <= 1) {
                expanded.add(item);
                continue;
            }
            int remainingRequestQty = item.getRequestQty() == null ? 0 : Math.abs(item.getRequestQty());
            if (remainingRequestQty <= 0) {
                throw fail("request qty must be greater than zero");
            }
            for (Long stockRecordId : stockRecordIds) {
                if (remainingRequestQty <= 0) {
                    break;
                }
                StockRecord record = requireAvailableOutboundRecord(stockRecordId);
                int availableQty = Math.max(availableOutboundQty(record), cartAddedQty(record));
                if (availableQty <= 0) {
                    continue;
                }
                int allocatedQty = Math.min(remainingRequestQty, availableQty);
                RequestFormWithItemsDTO.Item allocatedItem = new RequestFormWithItemsDTO.Item();
                BeanUtils.copyProperties(item, allocatedItem);
                allocatedItem.setId(null);
                allocatedItem.setStockRecordId(stockRecordId);
                allocatedItem.setStockRecordIds(null);
                allocatedItem.setStockOrderItemId(record.getOrderItemId());
                allocatedItem.setStockOrderItemIds(null);
                allocatedItem.setRequestQty(allocatedQty);
                expanded.add(allocatedItem);
                remainingRequestQty -= allocatedQty;
            }
            if (remainingRequestQty > 0) {
                throw fail("requested qty cannot exceed available outbound qty");
            }
        }
        return expanded;
    }

    private List<Long> stockRecordIdsOf(RequestFormWithItemsDTO.Item item) {
        if (item == null) {
            return List.of();
        }
        if (item.getStockRecordIds() != null && !item.getStockRecordIds().isEmpty()) {
            return item.getStockRecordIds().stream()
                    .filter(id -> id != null)
                    .toList();
        }
        return item.getStockRecordId() == null ? List.of() : List.of(item.getStockRecordId());
    }

    private int availableOutboundQty(StockRecord record) {
        StockOrderItem orderItem = stockOrderItemService.getByIdNotDeleted(record.getOrderItemId());
        return orderItem == null || orderItem.getChangeQty() == null ? 0 : Math.abs(orderItem.getChangeQty());
    }

    private int cartAddedQty(StockRecord record) {
        int originalQty = record == null || record.getChangeQty() == null ? 0 : Math.abs(record.getChangeQty());
        int remainingQty = record == null ? 0 : availableOutboundQty(record);
        return Math.max(0, originalQty - remainingQty);
    }

    private void prepareBundleItemFromStockRecord(RequestForm form,
                                                  RequestFormWithItemsDTO.Item item,
                                                  RequestItem existing) {
        if (item == null || item.getStockRecordId() == null) {
            return;
        }
        StockRecord record = requireAvailableOutboundRecord(item.getStockRecordId());
        if (form != null && form.getCustomerId() != null && record.getCustomerId() != null
                && !form.getCustomerId().equals(record.getCustomerId())) {
            throw fail("selected stock record customer does not match request form customer");
        }
        int requestedQty = item.getRequestQty() == null ? 0 : Math.abs(item.getRequestQty());
        if (requestedQty <= 0) {
            throw fail("request qty must be greater than zero");
        }
        int currentRequestQty = existing == null
                || !Integer.valueOf(StockBizConstant.REQUEST_ITEM_STATE_ADDED).equals(existing.getState())
                || existing.getRequestQty() == null ? cartAddedQty(record) : Math.abs(existing.getRequestQty());
        applyOutboundRemainderByRequested(record, requestedQty, currentRequestQty);
        applyStockRecordToBundleItem(item, record, requestedQty);
    }

    private void applyStockRecordToBundleItem(RequestFormWithItemsDTO.Item item, StockRecord record, int requestedQty) {
        item.setGoodsId(record.getGoodsId());
        item.setSkuId(record.getSkuId());
        item.setSkuCode(record.getSkuCode());
        item.setGoodsName(record.getGoodsName());
        item.setEnglishName(record.getEnglishName());
        item.setBrandId(record.getBrandId());
        item.setBrandName(record.getBrandName());
        item.setSeriesId(record.getSeriesId());
        item.setSeriesName(record.getSeriesName());
        item.setCategoryId(record.getCategoryId());
        item.setCategoryName(record.getCategoryName());
        item.setStockTypeId(record.getStockTypeId());
        item.setStockTypeName(record.getStockTypeName());
        item.setMakerId(record.getMakerId());
        item.setMakerName(record.getMakerName());
        item.setWarehouseId(record.getWarehouseId());
        item.setPrice(record.getPrice());
        if (item.getDiscount() == null) {
            item.setDiscount(BigDecimal.ONE);
        }
        if (item.getDiscountPrice() == null) {
            item.setDiscountPrice(record.getPrice());
        }
        item.setExchangeRate(item.getExchangeRate() == null ? BigDecimal.ONE : item.getExchangeRate());
        item.setCurrency(record.getCurrency() == null ? CommonConstant.DEFAULT_CURRENCY_JPY : record.getCurrency());
        item.setRequestQty(requestedQty);
        item.setOutQty(requestedQty);
        item.setState(StockBizConstant.REQUEST_ITEM_STATE_ADDED);
    }

    private void deleteItemsByRequestId(Long requestId) {
        List<RequestItem> items = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", requestId));
        for (RequestItem item : items) {
            requestItemService.deleteByIdLogic(item.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromOutbound(CreateRequestFromOutboundDTO dto) {
        StockOrder outboundOrder = stockOrderService.getByIdNotDeleted(dto.getStockOrderId());
        if (outboundOrder == null) {
            throw fail("source outbound order not found");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(outboundOrder.getOrderType())) {
            throw fail("source outbound order not found");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_STATE_FINISHED).equals(outboundOrder.getState())) {
            throw fail("source outbound order is not finished");
        }

        Long loginUserId = UserContext.getUserIdOrDefault();
        if (!loginUserId.equals(outboundOrder.getRequesterId()) && !loginUserId.equals(outboundOrder.getOperatorId())) {
            throw fail("source outbound order is not owned by current user");
        }

        List<StockOrderItem> allItems = stockOrderItemService.list(new QueryWrapper<StockOrderItem>()
                .eq("order_id", outboundOrder.getId()));
        if (allItems.isEmpty()) {
            throw fail("source outbound order has no items");
        }

        List<StockOrderItem> selectedItems = filterItems(allItems, dto.getStockOrderItemIds());
        if (selectedItems.isEmpty()) {
            throw fail("no source outbound items selected");
        }

        User user = userService.getByIdNotDeleted(loginUserId);
        Dept dept = user != null && user.getDeptId() != null ? deptService.getByIdNotDeleted(user.getDeptId()) : null;

        RequestForm form = new RequestForm();
        form.setBizNo(generateRequestNo());
        form.setUserId(loginUserId);
        form.setUsername(user == null ? null : user.getUsername());
        form.setDeptId(user == null ? null : user.getDeptId());
        form.setDeptName(dept == null ? null : dept.getName());
        form.setCustomerId(outboundOrder.getCustomerId());
        form.setCustomerName(outboundOrder.getCustomerName());
        form.setWarehouseId(outboundOrder.getWarehouseId());
        form.setSourceOrderId(outboundOrder.getId());
        form.setState(StockBizConstant.REQUEST_STATE_CREATED);
        form.setApproveRemark(dto.getRemark());

        int totalQty = 0;
        BigDecimal totalAmt = BigDecimal.ZERO;

        if (!this.save(form)) {
            throw fail("failed to save request form");
        }

        for (StockOrderItem orderItem : selectedItems) {
            StockRecord stockRecord = stockRecordService.getOne(new QueryWrapper<StockRecord>()
                    .eq("order_id", outboundOrder.getId())
                    .eq("order_item_id", orderItem.getId())
                    .eq("order_type", StockBizConstant.ORDER_TYPE_OUTBOUND)
                    .last("LIMIT 1"));
            if (stockRecord == null) {
                throw fail("source outbound stock record not found");
            }
            int requestQty = stockRecord.getChangeQty() == null ? 0 : Math.abs(stockRecord.getChangeQty());
            RequestItem requestItem = buildRequestItemFromStockRecord(form, stockRecord, dto.getRemark());
            requestItem.setRequestQty(requestQty);
            requestItem.setOutQty(requestQty);
            requestItem.setTotalAmt(safeAmount(stockRecord.getPrice()).multiply(BigDecimal.valueOf(requestQty)));
            applyOutboundRemainderDelta(stockRecord, requestQty);
            if (!requestItemService.save(requestItem)) {
                throw fail("failed to save request item");
            }

            totalQty += requestQty;
            totalAmt = totalAmt.add(safeAmount(requestItem.getTotalAmt()));
        }
        form.setTotalQty(totalQty);
        form.setRequestQty(totalQty);
        form.setTotalAmt(totalAmt);
        if (!this.updateById(form)) {
            throw fail("failed to update request form");
        }
        validateSourceBalance(form);
        return form.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromOutbound(StockOrder outboundOrder, List<Long> stockOrderItemIds, String remark) {
        if (outboundOrder == null) {
            throw fail("source outbound order not found");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(outboundOrder.getOrderType())) {
            throw fail("source outbound order not found");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_STATE_FINISHED).equals(outboundOrder.getState())) {
            throw fail("source outbound order is not finished");
        }

        Long ownerUserId = outboundOrder.getRequesterId() == null ? outboundOrder.getOperatorId() : outboundOrder.getRequesterId();
        User user = ownerUserId == null ? null : userService.getByIdNotDeleted(ownerUserId);
        Dept dept = user != null && user.getDeptId() != null ? deptService.getByIdNotDeleted(user.getDeptId()) : null;

        List<StockOrderItem> allItems = stockOrderItemService.list(new QueryWrapper<StockOrderItem>()
                .eq("order_id", outboundOrder.getId()));
        if (allItems.isEmpty()) {
            throw fail("source outbound order has no items");
        }

        List<StockOrderItem> selectedItems = filterItems(allItems, stockOrderItemIds);
        if (selectedItems.isEmpty()) {
            throw fail("no source outbound items selected");
        }

        RequestForm form = this.getOne(new QueryWrapper<RequestForm>()
                .eq("source_order_id", outboundOrder.getId())
                .last("LIMIT 1"));
        if (form == null) {
            form = new RequestForm();
            form.setBizNo(generateRequestNo());
            form.setUserId(ownerUserId);
            form.setUsername(user == null ? outboundOrder.getRequesterName() : user.getUsername());
            form.setDeptId(user == null ? null : user.getDeptId());
            form.setDeptName(dept == null ? null : dept.getName());
            form.setCustomerId(outboundOrder.getCustomerId());
            form.setCustomerName(outboundOrder.getCustomerName());
            form.setWarehouseId(outboundOrder.getWarehouseId());
            form.setSourceOrderId(outboundOrder.getId());
            form.setState(StockBizConstant.REQUEST_STATE_CREATED);
            form.setApproveRemark(remark);
            form.setTotalQty(0);
            form.setRequestQty(0);
            form.setTotalAmt(BigDecimal.ZERO);
            if (!this.save(form)) {
                throw fail("failed to save request form");
            }
        }

        for (StockOrderItem orderItem : selectedItems) {
            StockRecord stockRecord = stockRecordService.getOne(new QueryWrapper<StockRecord>()
                    .eq("order_id", outboundOrder.getId())
                    .eq("order_item_id", orderItem.getId())
                    .eq("order_type", StockBizConstant.ORDER_TYPE_OUTBOUND)
                    .last("LIMIT 1"));
            if (stockRecord == null) {
                throw fail("source outbound stock record not found");
            }
            int requestQty = stockRecord.getChangeQty() == null ? 0 : Math.abs(stockRecord.getChangeQty());
            RequestItem existing = requestItemService.getOne(new QueryWrapper<RequestItem>()
                    .eq("request_id", form.getId())
                    .eq("stock_record_id", stockRecord.getId())
                    .last("LIMIT 1"));
            if (existing == null) {
                RequestItem requestItem = buildRequestItemFromStockRecord(form, stockRecord, remark);
                requestItem.setRequestQty(requestQty);
                requestItem.setOutQty(requestQty);
                requestItem.setTotalAmt(safeAmount(stockRecord.getPrice()).multiply(BigDecimal.valueOf(requestQty)));
                applyOutboundRemainderDelta(stockRecord, requestQty);
                if (!requestItemService.save(requestItem)) {
                    throw fail("failed to save request item");
                }
            } else {
                int currentRequestQty = existing.getRequestQty() == null ? 0 : Math.abs(existing.getRequestQty());
                applyOutboundRemainderByRequested(stockRecord, requestQty, currentRequestQty);
                existing.setState(StockBizConstant.REQUEST_ITEM_STATE_ADDED);
                existing.setRequestQty(requestQty);
                existing.setOutQty(requestQty);
                existing.setPrice(stockRecord.getPrice());
                existing.setCurrency(stockRecord.getCurrency());
                existing.setTotalAmt(safeAmount(stockRecord.getPrice()).multiply(BigDecimal.valueOf(requestQty)));
                existing.setStockRecordId(stockRecord.getId());
                if (remark != null && !remark.isBlank()) {
                    existing.setRemark(remark);
                }
                if (!requestItemService.updateById(existing)) {
                    throw fail("failed to update request item");
                }
            }
        }
        form.setApproveRemark(remark);
        if (!this.updateById(form)) {
            throw fail("failed to update request form");
        }
        recalculateRequestFormSummary(form.getId());
        validateSourceBalance(form);
        return form.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reapplyInbound(Long requestId) {
        RequestForm form = this.getByIdNotDeleted(requestId);
        if (form == null) {
            throw fail("request form not found");
        }
        requireOwned(form);
        requireRequestFormEditable(form);
        if (Integer.valueOf(StockBizConstant.REQUEST_STATE_REINBOUND_APPLIED).equals(form.getState())) {
            throw fail("request form already reapplied inbound");
        }

        List<RequestItem> items = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", requestId)
                .eq("state", StockBizConstant.REQUEST_ITEM_STATE_ADDED)
                );
        if (items.isEmpty()) {
            throw fail("request form has no active items");
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
        inboundOrder.setRemark(REMARK_REAPPLY_INBOUND + form.getBizNo());
        inboundOrder.setBizDate(LocalDate.now());

        int totalQty = 0;
        for (RequestItem item : items) {
            totalQty += item.getRequestQty() == null ? 0 : item.getRequestQty();
        }
        inboundOrder.setTotalQty(totalQty);
        inboundOrder.setStockTypeId(items.get(0).getStockTypeId());
        if (!stockOrderService.save(inboundOrder)) {
            throw fail("failed to create inbound order");
        }

        for (RequestItem reqItem : items) {
            Stock stock = findStock(reqItem.getGoodsId(), reqItem.getSkuId(), reqItem.getWarehouseId(), reqItem.getStockTypeId());
            if (stock == null) {
                throw fail("stock not found for request item");
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
            orderItem.setRemark(REMARK_REAPPLY_INBOUND_ITEM);
            orderItem.setBizDate(LocalDate.now());
            if (!stockOrderItemService.save(orderItem)) {
                throw fail("failed to save inbound order item");
            }
        }

        form.setState(StockBizConstant.REQUEST_STATE_REINBOUND_APPLIED);
        if (!this.updateById(form)) {
            throw fail("failed to update request form");
        }
        return inboundOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reapplyInboundItem(RequestFormItemBatchDTO dto) {
        RequestForm form = this.getByIdNotDeleted(dto.getRequestId());
        if (form == null) {
            throw fail("request form not found");
        }
        requireOwned(form);
        requireRequestFormEditable(form);

        Map<Long, Integer> requestedQuantities = resolveRequestedQuantities(dto);
        if (requestedQuantities.size() != 1) {
            throw fail("no source outbound items selected");
        }

        Map.Entry<Long, Integer> entry = requestedQuantities.entrySet().iterator().next();
        StockRecord record = requireAvailableOutboundRecord(entry.getKey());
        int inboundQty = entry.getValue() == null ? 0 : Math.abs(entry.getValue());
        if (inboundQty <= 0) {
            throw fail("requested qty must be >= 0");
        }

        RequestItem requestItem = requestItemService.getOne(new QueryWrapper<RequestItem>()
                .eq("request_id", form.getId())
                .eq("stock_record_id", record.getId())
                .eq("state", StockBizConstant.REQUEST_ITEM_STATE_ADDED)
                .last("LIMIT 1"));
        if (requestItem == null) {
            throw fail("selected stock record is not available");
        }
        int requestQty = requestItem.getRequestQty() == null ? 0 : Math.abs(requestItem.getRequestQty());
        if (inboundQty > requestQty) {
            throw fail("requested qty cannot exceed available outbound qty");
        }

        StockOrder existed = findDeliveryInboundOrder(record.getId());
        if (existed != null) {
            return existed.getId();
        }

        Stock stock = findStock(record.getGoodsId(), record.getSkuId(), record.getWarehouseId(), record.getStockTypeId());
        if (stock == null) {
            throw fail("stock not found for request item");
        }

        Long loginUserId = UserContext.getUserIdOrDefault();
        User user = userService.getByIdNotDeleted(loginUserId);
        String username = user == null ? form.getUsername() : user.getUsername();

        StockOrder inboundOrder = new StockOrder();
        inboundOrder.setOrderNo(generateInboundNo());
        inboundOrder.setOrderType(StockBizConstant.ORDER_TYPE_INBOUND);
        inboundOrder.setWarehouseId(record.getWarehouseId());
        inboundOrder.setSourceType(StockBizConstant.SOURCE_TYPE_REQUEST);
        inboundOrder.setSourceId(record.getId());
        inboundOrder.setState(StockBizConstant.ORDER_STATE_APPROVING);
        inboundOrder.setRequesterId(loginUserId);
        inboundOrder.setRequesterName(username);
        inboundOrder.setOperatorId(loginUserId);
        inboundOrder.setOperatorName(username);
        inboundOrder.setRemark(REMARK_DELIVERY_INBOUND + record.getId() + ", request form: " + form.getBizNo());
        inboundOrder.setBizDate(LocalDate.now());
        inboundOrder.setTotalQty(inboundQty);
        inboundOrder.setStockTypeId(record.getStockTypeId());
        if (!stockOrderService.save(inboundOrder)) {
            throw fail("failed to create inbound order");
        }

        int beforeQty = stock.getCurrentQty() == null ? 0 : stock.getCurrentQty();
        StockOrderItem orderItem = new StockOrderItem();
        orderItem.setOrderId(inboundOrder.getId());
        orderItem.setGoodsId(record.getGoodsId());
        orderItem.setSkuId(record.getSkuId());
        orderItem.setSkuCode(record.getSkuCode());
        orderItem.setGoodsName(record.getGoodsName());
        orderItem.setEnglishName(record.getEnglishName());
        orderItem.setBrandId(record.getBrandId());
        orderItem.setBrandName(record.getBrandName());
        orderItem.setSeriesId(record.getSeriesId());
        orderItem.setSeriesName(record.getSeriesName());
        orderItem.setCategoryId(record.getCategoryId());
        orderItem.setCategoryName(record.getCategoryName());
        orderItem.setStockTypeId(record.getStockTypeId());
        orderItem.setStockTypeName(record.getStockTypeName());
        orderItem.setMakerId(record.getMakerId());
        orderItem.setMakerName(record.getMakerName());
        orderItem.setBeforeQty(beforeQty);
        orderItem.setChangeQty(inboundQty);
        orderItem.setAfterQty(beforeQty + inboundQty);
        orderItem.setPrice(record.getPrice());
        orderItem.setCurrency(record.getCurrency());
        orderItem.setRemark(REMARK_DELIVERY_INBOUND + record.getId());
        orderItem.setBizDate(LocalDate.now());
        if (!stockOrderItemService.save(orderItem)) {
            throw fail("failed to save inbound order item");
        }

        return inboundOrder.getId();
    }

    @Override
    public List<RequestCandidateItemVO> listCandidateItems(Long requestId) {
        RequestForm form = this.getByIdNotDeleted(requestId);
        if (form == null) {
            throw fail("request form not found");
        }
        requireOwned(form);

        List<StockRecord> records = listAvailableOutboundRecords();
        if (records.isEmpty()) {
            return new ArrayList<>();
        }

        List<RequestItem> existing = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", requestId));
        Map<Long, RequestItem> selectedMap = new HashMap<>();
        for (RequestItem requestItem : existing) {
            if (!Integer.valueOf(StockBizConstant.REQUEST_ITEM_STATE_ADDED).equals(requestItem.getState())) {
                continue;
            }
            if (requestItem.getStockRecordId() != null) {
                selectedMap.put(requestItem.getStockRecordId(), requestItem);
            }
        }

        List<RequestCandidateItemVO> result = new ArrayList<>();
        for (StockRecord record : records) {
            RequestItem selected = selectedMap.get(record.getId());
            RequestCandidateItemVO vo = new RequestCandidateItemVO();
            vo.setStockRecordId(record.getId());
            vo.setStockOrderId(record.getOrderId());
            vo.setStockOrderItemId(record.getOrderItemId());
            vo.setOrderNo(record.getBizNo());
            vo.setOrderType(record.getOrderType());
            vo.setState(StockBizConstant.ORDER_STATE_FINISHED);
            vo.setBizDate(record.getBizDate());
            vo.setGoodsId(record.getGoodsId());
            vo.setSkuId(record.getSkuId());
            vo.setSkuCode(record.getSkuCode());
            vo.setGoodsName(record.getGoodsName());
            vo.setBrandId(record.getBrandId());
            vo.setBrandName(record.getBrandName());
            vo.setSeriesId(record.getSeriesId());
            vo.setSeriesName(record.getSeriesName());
            vo.setMakerId(record.getMakerId());
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
            int originalQty = record.getChangeQty() == null ? 0 : Math.abs(record.getChangeQty());
            int availableQty = Math.min(originalQty, remainingQty + selectedQty);
            vo.setChangeQty(availableQty);
            vo.setAvailableQty(availableQty);
            vo.setPrice(record.getPrice());
            vo.setCurrency(record.getCurrency());
            vo.setRequesterName(record.getRequesterName());
            vo.setOperatorName(record.getOperatorName());
            vo.setSelected(selected != null);
            vo.setRequestQty(selectedQty);
            vo.setAmount(safeAmount(record.getPrice()).multiply(BigDecimal.valueOf(selectedQty)));
            vo.setRequestItemState(selected == null ? null : selected.getState());
            vo.setRequestItemId(selected == null ? null : selected.getId());
            StockOrder inboundOrder = findDeliveryInboundOrder(record.getId());
            vo.setInboundApplied(inboundOrder != null);
            vo.setInboundOrderId(inboundOrder == null ? null : inboundOrder.getId());
            vo.setKnife(isKnifeCategory(record.getCategoryName()));
            vo.setHandle(isHandleCategory(record.getCategoryName()));
            result.add(vo);
        }
        fillHandleCandidates(result);
        return result;
    }

    @Override
    public PageResult<RequestCandidateItemVO> pageCartItems(RequestItemCartQueryDTO query) {
        List<RequestCandidateItemVO> candidates = listMergedCartCandidates(query);
        long total = candidates.size();
        long pageNum = query.getPageNum();
        long pageSize = query.getPageSize();
        int from = (int) Math.min(total, Math.max(0, (pageNum - 1) * pageSize));
        int to = (int) Math.min(total, from + pageSize);
        return PageResult.build(total, pageNum, pageSize, candidates.subList(from, to));
    }

    @Override
    public RequestFormCartPreviewVO previewCartItems(RequestItemCartQueryDTO query) {
        List<RequestCandidateItemVO> candidates = listMergedCartCandidates(query);
        Customer customer = resolvePreviewCustomer(query, candidates);
        String templateCode = resolveCartPreviewTemplateCode(query, candidates);

        RequestFormCartPreviewVO preview = new RequestFormCartPreviewVO();
        preview.setTemplateCode(templateCode);
        preview.setInvoiceDate(LocalDate.now());
        preview.setCompany(buildPreviewCompany());
        preview.setCustomer(buildPreviewCustomer(customer, query, candidates));
        preview.setShipping(buildPreviewShipping());
        preview.setBank(buildPreviewBank());
        preview.setColumns(List.of("No.", "Brand", "Item", "Qty", "Unit price", "Price", "Remark", "HS Code"));

        int totalQty = 0;
        BigDecimal totalAmt = BigDecimal.ZERO;
        String currency = null;
        List<RequestFormCartPreviewVO.Item> items = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            RequestCandidateItemVO candidate = candidates.get(i);
            RequestFormCartPreviewVO.Item item = buildPreviewItem(i + 1, candidate);
            items.add(item);
            totalQty += safeQty(item.getQty());
            totalAmt = totalAmt.add(safeAmount(item.getPrice()));
            if (currency == null || currency.isBlank()) {
                currency = item.getCurrency();
            }
        }
        preview.setItems(items);
        preview.setTotalQty(totalQty);
        preview.setTotalAmt(totalAmt);
        preview.setCurrency(currency == null || currency.isBlank() ? CommonConstant.DEFAULT_CURRENCY_JPY : currency);
        return preview;
    }

    private List<RequestCandidateItemVO> listMergedCartCandidates(RequestItemCartQueryDTO query) {
        List<StockRecord> records = listCartOutboundRecords(query);
        List<RequestCandidateItemVO> candidates = new ArrayList<>();
        for (StockRecord record : records) {
            RequestCandidateItemVO candidate = buildCartCandidate(record);
            if (candidate != null) {
                candidates.add(candidate);
            }
        }
        return mergeCartCandidates(candidates);
    }

    private Customer resolvePreviewCustomer(RequestItemCartQueryDTO query, List<RequestCandidateItemVO> candidates) {
        Long customerId = query == null ? null : query.getCustomerId();
        if (customerId == null && !candidates.isEmpty()) {
            customerId = candidates.get(0).getCustomerId();
        }
        if (customerId == null) {
            return null;
        }
        validateCustomerOwnership(customerId, UserContext.getUserIdOrDefault());
        return customerService.getByIdNotDeleted(customerId);
    }

    private String resolveCartPreviewTemplateCode(RequestItemCartQueryDTO query, List<RequestCandidateItemVO> candidates) {
        String groupCode = query == null ? null : query.getGroupCode();
        if ((groupCode == null || groupCode.isBlank()) && !candidates.isEmpty()) {
            groupCode = candidates.get(0).getGroupCode();
        }
        String code = normalizeTemplateCode(groupCode);
        if (TEMPLATE_CODE_B.equals(code)) {
            return TEMPLATE_CODE_B;
        }
        if (TEMPLATE_CODE_C.equals(code)) {
            return TEMPLATE_CODE_C;
        }
        return TEMPLATE_CODE_A;
    }

    private RequestFormCartPreviewVO.CompanyInfo buildPreviewCompany() {
        RequestFormCartPreviewVO.CompanyInfo company = new RequestFormCartPreviewVO.CompanyInfo();
        company.setName("H&K CO., LTD.(カブシキガイシャ エイチアンドケー)");
        company.setTel("(06)6439-6361");
        company.setEmail("cho@handk.co");
        company.setAddress("Amagasaki K.R Bld.1F, 3-90-1, Showadori, Amagasaki City, Hyogo Prefecture, 660-0881, Japan");
        return company;
    }

    private RequestFormCartPreviewVO.CustomerInfo buildPreviewCustomer(Customer customer,
                                                                       RequestItemCartQueryDTO query,
                                                                       List<RequestCandidateItemVO> candidates) {
        RequestFormCartPreviewVO.CustomerInfo info = new RequestFormCartPreviewVO.CustomerInfo();
        if (customer != null) {
            info.setId(customer.getId());
            info.setCode(customer.getCustomerCode());
            info.setName(customer.getName());
            info.setEnglishName(customer.getEnglishName());
            info.setContactPerson(customer.getContactPerson());
            info.setPhone(customer.getPhone());
            info.setEmail(customer.getEmail());
            info.setCountry(customer.getCountry());
            info.setCity(customer.getCity());
            info.setAddress(customer.getAddress());
            return info;
        }
        if (query != null) {
            info.setId(query.getCustomerId());
            info.setName(query.getCustomerName());
        }
        if ((info.getId() == null || info.getName() == null) && !candidates.isEmpty()) {
            RequestCandidateItemVO first = candidates.get(0);
            info.setId(first.getCustomerId());
            info.setName(first.getCustomerName());
            info.setCountry(first.getCountry());
        }
        return info;
    }

    private RequestFormCartPreviewVO.ShippingInfo buildPreviewShipping() {
        RequestFormCartPreviewVO.ShippingInfo shipping = new RequestFormCartPreviewVO.ShippingInfo();
        shipping.setTerm("FOB");
        shipping.setCountryOfOrigin("Japan");
        shipping.setTransportedFrom("Osaka JAPAN");
        return shipping;
    }

    private RequestFormCartPreviewVO.BankInfo buildPreviewBank() {
        RequestFormCartPreviewVO.BankInfo bank = new RequestFormCartPreviewVO.BankInfo();
        bank.setTitle("Bank information");
        bank.setBankName("Resona Bank, Ltd.");
        bank.setBankAddress("1-5-25 KIBA KOTO TOKYO JAPAN");
        bank.setSwiftCode("DIWAJPJT");
        bank.setAccountNo("(IBAN) 1532589");
        bank.setAccountType("Savings account(Futsu)");
        bank.setBranchNo("528");
        bank.setBranchName("NISHINOMIYAKITAGUCHI Branch");
        bank.setBeneficiaryName("H AND K CO.,LTD");
        bank.setBeneficiaryAddress("Amagasaki K.R Bld.1F, 3-90-1, Showadori, Amagasaki City, Hyogo Prefecture, 660-0881, Japan");
        bank.setNotice("When total amount of order is less than 1 million yen,the bank transfer fee will be borne by the customer.");
        return bank;
    }

    private RequestFormCartPreviewVO.Item buildPreviewItem(int no, RequestCandidateItemVO candidate) {
        RequestFormCartPreviewVO.Item item = new RequestFormCartPreviewVO.Item();
        item.setNo(no);
        item.setBrandName(candidate.getBrandName());
        item.setGoodsName(candidate.getGoodsName());
        item.setSkuCode(candidate.getSkuCode());
        item.setSeriesName(candidate.getSeriesName());
        item.setMakerName(candidate.getMakerName());
        item.setCategoryName(candidate.getCategoryName());
        item.setStockTypeName(candidate.getStockTypeName());
        item.setBizDate(candidate.getBizDate());
        item.setItemName(buildCandidateItemDescription(candidate));
        item.setQty(safeQty(candidate.getRequestQty()));
        item.setUnitPrice(candidate.getPrice() == null ? BigDecimal.ZERO : candidate.getPrice());
        item.setPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQty())));
        item.setCurrency(candidate.getCurrency() == null ? CommonConstant.DEFAULT_CURRENCY_JPY : candidate.getCurrency());
        item.setRemark("");
        item.setHsCode("");
        item.setStockRecordId(candidate.getStockRecordId());
        item.setStockRecordIds(candidate.getStockRecordIds());
        item.setStockOrderItemId(candidate.getStockOrderItemId());
        item.setStockOrderItemIds(candidate.getStockOrderItemIds());
        return item;
    }

    private String buildCandidateItemDescription(RequestCandidateItemVO candidate) {
        List<String> parts = new ArrayList<>();
        addPart(parts, candidate.getGoodsName());
        addPart(parts, candidate.getSkuCode());
        addPart(parts, candidate.getSeriesName());
        addPart(parts, candidate.getCategoryName());
        addPart(parts, candidate.getMakerName());
        addPart(parts, candidate.getStockTypeName());
        return String.join(" / ", parts);
    }

    private List<StockRecord> listCartOutboundRecords(RequestItemCartQueryDTO query) {
        QueryWrapper<StockRecord> wrapper = new QueryWrapper<StockRecord>()
                .eq("order_type", StockBizConstant.ORDER_TYPE_OUTBOUND)
                .in("outbound_mode", StockBizConstant.OUTBOUND_MODE_CUSTOMER, StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER)
                .isNotNull("customer_id")
                .orderByDesc("biz_date", "id");
        if (query.getCustomerId() != null) {
            wrapper.eq("customer_id", query.getCustomerId());
        }
        if (query.getCustomerName() != null && !query.getCustomerName().isBlank()) {
            wrapper.like("customer_name", query.getCustomerName().trim());
        }
        if (query.getGroupCode() != null && !query.getGroupCode().isBlank()) {
            wrapper.apply("UPPER(dept_code) = {0}", query.getGroupCode().trim().toUpperCase(Locale.ROOT));
        }
        if (query.getGoodsId() != null) {
            wrapper.eq("goods_id", query.getGoodsId());
        }
        if (query.getGoodsName() != null && !query.getGoodsName().isBlank()) {
            wrapper.like("goods_name", query.getGoodsName().trim());
        }
        if (query.getSkuId() != null) {
            wrapper.eq("sku_id", query.getSkuId());
        }
        if (query.getSkuCode() != null && !query.getSkuCode().isBlank()) {
            wrapper.like("sku_code", query.getSkuCode().trim());
        }
        if (query.getStockTypeId() != null) {
            wrapper.eq("stock_type_id", query.getStockTypeId());
        }
        if (query.getStartDate() != null) {
            wrapper.ge("biz_date", query.getStartDate());
        }
        if (query.getEndDate() != null) {
            wrapper.le("biz_date", query.getEndDate());
        }

        List<StockRecord> records = stockRecordService.list(wrapper);
        if (records.isEmpty()) {
            return records;
        }
        Set<Long> orderIds = new HashSet<>();
        for (StockRecord record : records) {
            if (record.getOrderId() != null) {
                orderIds.add(record.getOrderId());
            }
        }
        Set<Long> finishedOrderIds = new HashSet<>();
        if (!orderIds.isEmpty()) {
            for (StockOrder order : stockOrderService.list(new QueryWrapper<StockOrder>()
                    .in("id", orderIds)
                    .eq("order_type", StockBizConstant.ORDER_TYPE_OUTBOUND)
                    .eq("state", StockBizConstant.ORDER_STATE_FINISHED))) {
                finishedOrderIds.add(order.getId());
            }
        }
        Long userId = UserContext.getUserIdOrDefault();
        return records.stream()
                .filter(record -> finishedOrderIds.contains(record.getOrderId()))
                .filter(record -> canAccessCustomerRecord(record, userId))
                .toList();
    }

    private RequestCandidateItemVO buildCartCandidate(StockRecord record) {
        int addedQty = cartAddedQty(record);
        if (addedQty <= 0) {
            return null;
        }
        RequestCandidateItemVO vo = new RequestCandidateItemVO();
        vo.setStockRecordId(record.getId());
        vo.setStockRecordIds(new ArrayList<>(List.of(record.getId())));
        vo.setStockOrderId(record.getOrderId());
        vo.setStockOrderItemId(record.getOrderItemId());
        if (record.getOrderItemId() != null) {
            vo.setStockOrderItemIds(new ArrayList<>(List.of(record.getOrderItemId())));
        }
        vo.setOrderNo(record.getBizNo());
        vo.setOrderType(record.getOrderType());
        vo.setState(StockBizConstant.ORDER_STATE_FINISHED);
        vo.setBizDate(record.getBizDate());
        vo.setCountry(resolveCustomerCountry(record.getCustomerId()));
        vo.setGroupCode(record.getDeptCode());
        vo.setCustomerId(record.getCustomerId());
        vo.setCustomerName(record.getCustomerName());
        vo.setGoodsId(record.getGoodsId());
        vo.setSkuId(record.getSkuId());
        vo.setSkuCode(record.getSkuCode());
        vo.setGoodsName(record.getGoodsName());
        vo.setBrandId(record.getBrandId());
        vo.setBrandName(record.getBrandName());
        vo.setSeriesId(record.getSeriesId());
        vo.setSeriesName(record.getSeriesName());
        vo.setMakerId(record.getMakerId());
        vo.setCategoryName(record.getCategoryName());
        vo.setStockTypeName(record.getStockTypeName());
        vo.setMakerName(record.getMakerName());
        vo.setChangeQty(addedQty);
        vo.setAvailableQty(addedQty);
        vo.setPrice(record.getPrice());
        vo.setCurrency(record.getCurrency());
        vo.setRequesterName(record.getRequesterName());
        vo.setOperatorName(record.getOperatorName());
        vo.setSelected(false);
        vo.setRequestQty(addedQty);
        vo.setAmount(safeAmount(record.getPrice()).multiply(BigDecimal.valueOf(addedQty)));
        vo.setKnife(isKnifeCategory(record.getCategoryName()));
        vo.setHandle(isHandleCategory(record.getCategoryName()));
        return vo;
    }

    private List<RequestCandidateItemVO> mergeCartCandidates(List<RequestCandidateItemVO> candidates) {
        if (candidates.isEmpty()) {
            return candidates;
        }
        Map<String, RequestCandidateItemVO> merged = new LinkedHashMap<>();
        for (RequestCandidateItemVO candidate : candidates) {
            String key = cartMergeKey(candidate);
            RequestCandidateItemVO existing = merged.get(key);
            if (existing == null) {
                merged.put(key, candidate);
                continue;
            }
            int availableQty = safeQty(existing.getAvailableQty()) + safeQty(candidate.getAvailableQty());
            existing.setAvailableQty(availableQty);
            existing.setChangeQty(safeQty(existing.getChangeQty()) + safeQty(candidate.getChangeQty()));
            existing.setRequestQty(availableQty);
            existing.setAmount(safeAmount(existing.getAmount()).add(safeAmount(candidate.getAmount())));
            List<Long> stockRecordIds = new ArrayList<>(existing.getStockRecordIds() == null
                    ? List.of(existing.getStockRecordId())
                    : existing.getStockRecordIds());
            if (candidate.getStockRecordIds() != null && !candidate.getStockRecordIds().isEmpty()) {
                stockRecordIds.addAll(candidate.getStockRecordIds());
            } else if (candidate.getStockRecordId() != null) {
                stockRecordIds.add(candidate.getStockRecordId());
            }
            existing.setStockRecordIds(stockRecordIds);

            List<Long> stockOrderItemIds = new ArrayList<>(existing.getStockOrderItemIds() == null
                    ? List.of()
                    : existing.getStockOrderItemIds());
            if (candidate.getStockOrderItemIds() != null && !candidate.getStockOrderItemIds().isEmpty()) {
                stockOrderItemIds.addAll(candidate.getStockOrderItemIds());
            } else if (candidate.getStockOrderItemId() != null) {
                stockOrderItemIds.add(candidate.getStockOrderItemId());
            }
            existing.setStockOrderItemIds(stockOrderItemIds);
            existing.setOrderNo("複数");
        }
        return new ArrayList<>(merged.values());
    }

    private String cartMergeKey(RequestCandidateItemVO item) {
        return String.join("|",
                String.valueOf(item.getCustomerId()),
                String.valueOf(item.getBizDate()),
                String.valueOf(item.getGoodsId()),
                String.valueOf(item.getSkuId()),
                String.valueOf(item.getStockTypeName()),
                String.valueOf(item.getPrice() == null ? "" : item.getPrice().stripTrailingZeros().toPlainString()),
                String.valueOf(item.getCurrency()));
    }

    private int safeQty(Integer qty) {
        return qty == null ? 0 : Math.abs(qty);
    }

    private String resolveCustomerCountry(Long customerId) {
        if (customerId == null) {
            return null;
        }
        Customer customer = customerService.getByIdNotDeleted(customerId);
        if (customer == null || customer.getCountry() == null || customer.getCountry().isBlank()) {
            return "未設定";
        }
        return customer.getCountry();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addItemsToCart(RequestCartMoveDTO dto) {
        if (dto == null || dto.getItems() == null) {
            return true;
        }
        for (RequestCartMoveDTO.Item item : dto.getItems()) {
            allocateCartMove(dto, item, true);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeItemsFromCart(RequestCartMoveDTO dto) {
        if (dto == null || dto.getItems() == null) {
            return true;
        }
        for (RequestCartMoveDTO.Item item : dto.getItems()) {
            allocateCartMove(dto, item, false);
        }
        return true;
    }

    private void allocateCartMove(RequestCartMoveDTO dto, RequestCartMoveDTO.Item item, boolean addToCart) {
        if (item == null) {
            return;
        }
        int remainingQty = item.getRequestQty() == null ? 0 : item.getRequestQty();
        if (remainingQty <= 0) {
            throw fail("request qty must be greater than zero");
        }
        List<Long> sourceIds = cartMoveStockRecordIds(item);
        if (sourceIds.isEmpty()) {
            throw fail("stock record is required");
        }
        for (Long stockRecordId : sourceIds) {
            if (remainingQty <= 0) {
                break;
            }
            StockRecord record = requireCartMoveRecord(dto, stockRecordId);
            int movableQty = addToCart ? availableOutboundQty(record) : cartAddedQty(record);
            if (movableQty <= 0) {
                continue;
            }
            int allocatedQty = Math.min(remainingQty, movableQty);
            applyOutboundRemainderDelta(record, addToCart ? allocatedQty : -allocatedQty);
            validateCartMoveBalance(record);
            remainingQty -= allocatedQty;
        }
        if (remainingQty > 0) {
            throw fail((addToCart ? "add qty cannot exceed delivery schedule qty" : "return qty cannot exceed request item qty")
                    + ", requested=" + item.getRequestQty()
                    + ", remaining=" + remainingQty);
        }
    }

    private List<Long> cartMoveStockRecordIds(RequestCartMoveDTO.Item item) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        if (item.getStockRecordIds() != null) {
            for (Long id : item.getStockRecordIds()) {
                if (id != null && id > 0) {
                    ids.add(id);
                }
            }
        }
        if (item.getStockRecordId() != null && item.getStockRecordId() > 0) {
            ids.add(item.getStockRecordId());
        }
        return new ArrayList<>(ids);
    }

    private StockRecord requireCartMoveRecord(RequestCartMoveDTO dto, Long stockRecordId) {
        StockRecord record = requireAvailableOutboundRecord(stockRecordId);
        if (dto != null && dto.getCustomerId() != null && record.getCustomerId() != null
                && !dto.getCustomerId().equals(record.getCustomerId())) {
            throw fail("selected stock record customer does not match request customer");
        }
        return record;
    }

    private void fillHandleCandidates(List<RequestCandidateItemVO> candidates) {
        List<RequestCandidateItemVO> handles = candidates.stream()
                .filter(item -> Boolean.TRUE.equals(item.getHandle()))
                .toList();
        if (handles.isEmpty()) {
            return;
        }
        for (RequestCandidateItemVO item : candidates) {
            if (!Boolean.TRUE.equals(item.getKnife())) {
                continue;
            }
            List<RequestCandidateItemVO> matchedHandles = handles.stream()
                    .filter(handle -> isPreferredHandleForKnife(item, handle))
                    .toList();
            item.setHandleCandidates(matchedHandles.isEmpty() ? handles : matchedHandles);
        }
    }

    private boolean isPreferredHandleForKnife(RequestCandidateItemVO knife, RequestCandidateItemVO handle) {
        return sameLong(knife.getBrandId(), handle.getBrandId())
                || sameLong(knife.getSeriesId(), handle.getSeriesId())
                || sameLong(knife.getMakerId(), handle.getMakerId())
                || sameText(knife.getBrandName(), handle.getBrandName())
                || sameText(knife.getSeriesName(), handle.getSeriesName())
                || sameText(knife.getMakerName(), handle.getMakerName());
    }

    private boolean sameLong(Long left, Long right) {
        return left != null && left.equals(right);
    }

    private boolean sameText(String left, String right) {
        return left != null && !left.isBlank() && left.equals(right);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addItemsFromStockOrder(RequestFormItemBatchDTO dto) {
        RequestForm form = this.getByIdNotDeleted(dto.getRequestId());
        if (form == null) {
            throw fail("request form not found");
        }
        requireOwned(form);
        requireRequestFormEditable(form);
        Map<Long, Integer> requestedQuantities = resolveRequestedQuantities(dto);
        if (requestedQuantities.isEmpty()) {
            return true;
        }
        for (Map.Entry<Long, Integer> entry : requestedQuantities.entrySet()) {
            Long stockRecordId = entry.getKey();
            int requestedQty = entry.getValue();
            StockRecord stockRecord = requireAvailableOutboundRecord(stockRecordId);

            RequestItem existing = requestItemService.getOne(new QueryWrapper<RequestItem>()
                    .eq("request_id", form.getId())
                    .eq("stock_record_id", stockRecordId)
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
                    throw fail("failed to add request item");
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
                    throw fail("failed to update request item");
                }
            }
        }

        validateKnifeHandleQuantity(form.getId());
        recalculateRequestFormSummary(form.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean matchItemsFromStockOrder(RequestFormItemBatchDTO dto) {
        RequestForm form = this.getByIdNotDeleted(dto.getRequestId());
        if (form == null) {
            throw fail("request form not found");
        }
        requireOwned(form);
        requireRequestFormEditable(form);

        Map<Long, Integer> requestedQuantities = resolveRequestedQuantities(dto);
        List<RequestItem> activeItems = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", form.getId())
                .eq("state", StockBizConstant.REQUEST_ITEM_STATE_ADDED)
                );
        List<Long> removedStockRecordIds = new ArrayList<>();
        for (RequestItem item : activeItems) {
            if (item.getStockRecordId() != null && !requestedQuantities.containsKey(item.getStockRecordId())) {
                removedStockRecordIds.add(item.getStockRecordId());
            }
        }
        if (!removedStockRecordIds.isEmpty()) {
            RequestFormItemBatchDTO removeDto = new RequestFormItemBatchDTO();
            removeDto.setRequestId(form.getId());
            removeDto.setStockOrderItemIds(removedStockRecordIds);
            removeItemsFromRequest(removeDto);
        }
        if (!requestedQuantities.isEmpty()) {
            addItemsFromStockOrder(dto);
        }
        validateKnifeHandleQuantity(form.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeItemsFromRequest(RequestFormItemBatchDTO dto) {
        RequestForm form = this.getByIdNotDeleted(dto.getRequestId());
        if (form == null) {
            throw fail("request form not found");
        }
        requireOwned(form);
        requireRequestFormEditable(form);
        List<Long> stockRecordIds = !normalizeStockRecordIds(dto.getStockOrderItemIds()).isEmpty()
                ? normalizeStockRecordIds(dto.getStockOrderItemIds())
                : new ArrayList<>(resolveRequestedQuantities(dto).keySet());
        if (stockRecordIds.isEmpty()) {
            return true;
        }

        List<RequestItem> existing = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", form.getId())
                .eq("state", StockBizConstant.REQUEST_ITEM_STATE_ADDED)
                .in("stock_record_id", stockRecordIds));
        if (existing == null || existing.isEmpty()) {
            return true;
        }

        for (RequestItem requestItem : existing) {
            int currentQty = requestItem.getRequestQty() == null ? 0 : Math.abs(requestItem.getRequestQty());
            StockRecord record = requireAvailableOutboundRecord(requestItem.getStockRecordId());
            applyOutboundRemainderByRequested(record, 0, currentQty);
            boolean removed = requestItemService.update(new LambdaUpdateWrapper<RequestItem>()
                    .eq(RequestItem::getId, requestItem.getId())
                    .eq(RequestItem::getState, StockBizConstant.REQUEST_ITEM_STATE_ADDED)
                    .set(RequestItem::getState, StockBizConstant.REQUEST_ITEM_STATE_REMOVED));
            if (!removed) {
                throw fail("failed to remove request item");
            }
        }
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
            throw fail("request form not found");
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
            throw fail("request form workbook generation failed", e);
        }
    }

    private void downloadRequestFormPdf(Long requestId, HttpServletResponse response) {
        RequestForm form = this.getByIdNotDeleted(requestId);
        if (form == null) {
            throw fail("request form not found");
        }

        try (XSSFWorkbook workbook = buildRequestWorkbook(form);
             PDDocument document = new PDDocument()) {
            writeRequestPdf(document, workbook);
            String filename = "request_" + form.getBizNo() + ".pdf";
            response.setContentType("application/pdf");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            document.save(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw fail("request form pdf generation failed", e);
        }
    }

    private XSSFWorkbook buildRequestWorkbook(RequestForm form) throws IOException {
        List<RequestItem> items = listRequestItems(form.getId());
        Customer customer = form.getCustomerId() == null ? null : customerService.getByIdNotDeleted(form.getCustomerId());
        String templateCode = resolveTemplateCode(form);
        XSSFWorkbook workbook;
        try (InputStream templateInput = openTemplateInputStream(form)) {
            workbook = new XSSFWorkbook(templateInput);
        }
        workbook.setForceFormulaRecalculation(true);
        Sheet sheet = workbook.getSheetAt(0);
        workbook.setSheetName(0, safeSheetName(form.getBizNo()));
        while (workbook.getNumberOfSheets() > 1) {
            workbook.removeSheetAt(workbook.getNumberOfSheets() - 1);
        }
        fillHeader(sheet, form, customer, templateCode);
        fillItems(sheet, items, templateCode);
        return workbook;
    }

    private List<RequestItem> listRequestItems(Long requestId) {
        List<RequestItem> items = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", requestId));
        if (items == null || items.isEmpty()) {
            throw fail("request form has no active items");
        }
        return items;
    }

    private void writeRequestPdf(PDDocument document, XSSFWorkbook workbook) throws IOException {
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter formatter = new DataFormatter();
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        int minRow = Integer.MAX_VALUE;
        int maxRow = -1;
        int maxCol = -1;
        for (int r = 0; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            short rowLastCell = row.getLastCellNum();
            if (rowLastCell <= 0) {
                continue;
            }
            for (int c = 0; c < rowLastCell; c++) {
                Cell cell = row.getCell(c);
                String text = cell == null ? "" : formatter.formatCellValue(cell, evaluator);
                if (text != null && !text.isBlank()) {
                    minRow = Math.min(minRow, r);
                    maxRow = Math.max(maxRow, r);
                    maxCol = Math.max(maxCol, c);
                }
            }
        }
        if (maxRow < 0 || maxCol < 0) {
            PDPage emptyPage = new PDPage(PDRectangle.A4);
            document.addPage(emptyPage);
            return;
        }

        final float marginX = 24F;
        final float marginY = 24F;
        final float pageWidth = PDRectangle.A4.getWidth();
        final float pageHeight = PDRectangle.A4.getHeight();
        final float printableWidth = pageWidth - marginX * 2F;
        final float printableHeight = pageHeight - marginY * 2F;
        final float fontSize = 6.5F;
        final float textPaddingX = 1.5F;
        final float textPaddingY = 1.5F;

        float[] colWidths = new float[maxCol + 1];
        float totalWidth = 0F;
        for (int c = 0; c <= maxCol; c++) {
            float width = (float) sheet.getColumnWidthInPixels(c);
            if (width <= 0F) {
                width = 24F;
            }
            colWidths[c] = width;
            totalWidth += width;
        }
        float scale = totalWidth <= 0F ? 1F : printableWidth / totalWidth;
        if (scale > 1F) {
            scale = 1F;
        }

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream content = new PDPageContentStream(document, page);
        float yTop = pageHeight - marginY;
        PDFont font = PDType1Font.HELVETICA;

        for (int r = minRow; r <= maxRow; r++) {
            Row row = sheet.getRow(r);
            float rowHeight = row == null ? sheet.getDefaultRowHeightInPoints() : row.getHeightInPoints();
            if (rowHeight <= 0F) {
                rowHeight = sheet.getDefaultRowHeightInPoints();
            }
            rowHeight *= scale;
            if (yTop - rowHeight < marginY) {
                content.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                content = new PDPageContentStream(document, page);
                yTop = pageHeight - marginY;
            }
            float x = marginX;
            for (int c = 0; c <= maxCol; c++) {
                float cellWidth = colWidths[c] * scale;
                content.addRect(x, yTop - rowHeight, cellWidth, rowHeight);
                content.stroke();

                String text = "";
                if (row != null) {
                    Cell cell = row.getCell(c);
                    text = cell == null ? "" : formatter.formatCellValue(cell, evaluator);
                }
                if (text != null && !text.isBlank()) {
                    content.beginText();
                    content.setFont(font, fontSize);
                    content.newLineAtOffset(x + textPaddingX, yTop - rowHeight + textPaddingY);
                    content.showText(sanitizePdfText(text));
                    content.endText();
                }
                x += cellWidth;
            }
            yTop -= rowHeight;
        }
        content.close();
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
        return getTemplatePathByCode(resolveTemplateCode(form));
    }

    private String resolveTemplatePathFromConfig(RequestForm form) {
        String configKey = resolveTemplateConfigKeyByCode(resolveTemplateCode(form));
        String value = getConfigValueByName(configKey);
        if (value == null || value.isBlank()) {
            value = getConfigValueByName(CONFIG_TEMPLATE_DEFAULT);
        }
        return normalizeTemplatePath(value);
    }

    private String resolveTemplateCode(RequestForm form) {
        Long deptId = form == null ? null : form.getDeptId();
        if (deptId != null) {
            Dept dept = deptService.getByIdNotDeleted(deptId);
            String code = normalizeTemplateCode(dept == null ? null : dept.getCode());
            if (code != null) {
                return code;
            }
        }
        String deptName = form == null || form.getDeptName() == null ? "" : form.getDeptName().toUpperCase();
        if (deptName.contains("A")) {
            return TEMPLATE_CODE_A;
        }
        if (deptName.contains("B")) {
            return TEMPLATE_CODE_B;
        }
        if (deptName.contains("C")) {
            return TEMPLATE_CODE_C;
        }
        return TEMPLATE_CODE_A;
    }

    private String resolveTemplateConfigKeyByCode(String code) {
        return "request.form.template." + normalizeTemplateCode(code);
    }

    private String getTemplatePathByCode(String code) {
        if (TEMPLATE_CODE_B.equals(code)) {
            return TEMPLATE_B;
        }
        if (TEMPLATE_CODE_C.equals(code)) {
            return TEMPLATE_C;
        }
        return TEMPLATE_A;
    }

    private String normalizeTemplateCode(String code) {
        if (code == null) {
            return null;
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }

    private String getConfigValueByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        Config config = configService.getOne(new QueryWrapper<Config>()
                .eq("name", name)
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
            if (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }
        }
        return normalized;
    }

    private void fillHeader(Sheet sheet, RequestForm form, Customer customer, String templateCode) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-M-d"));
        if (TEMPLATE_CODE_B.equals(templateCode)) {
            setCellText(sheet, 5, 1, "MESSRS: " + customerName(form, customer));
            setCellText(sheet, 5, 7, today);
            setCellText(sheet, 6, 1, "Address: " + safe(customer == null ? null : customer.getAddress()));
            setCellText(sheet, 7, 1, "Tel: " + safe(customer == null ? null : customer.getPhone()));
            setCellText(sheet, 8, 1, "EMAIL: " + safe(customer == null ? null : customer.getEmail()));
            return;
        }
        if (TEMPLATE_CODE_C.equals(templateCode)) {
            setCellText(sheet, 3, 1, customerName(form, customer));
            setCellText(sheet, 3, 6, today);
            setCellText(sheet, 4, 1, safe(customer == null ? null : customer.getContactPerson()));
            setCellText(sheet, 5, 1, safe(customer == null ? null : customer.getAddress()));
            setCellText(sheet, 6, 1, contactLine(customer));
            return;
        }
        setCellText(sheet, 3, 1, customerName(form, customer));
        setCellText(sheet, 3, 11, today);
        if (customer != null) {
            setCellText(sheet, 4, 2, safe(customer.getContactPerson()));
            setCellText(sheet, 6, 1, safe(customer.getAddress()));
            setCellText(sheet, 7, 1, contactLine(customer));
        }
    }

    private void fillItems(Sheet sheet, List<RequestItem> items, String templateCode) {
        ItemLayout layout = itemLayout(templateCode);
        for (int r = layout.startRow(); r <= layout.endRow(); r++) {
            for (int c = layout.firstCol(); c <= layout.lastCol(); c++) {
                setCellText(sheet, r, c, "");
            }
        }
        int limit = Math.min(items.size(), layout.capacity());
        for (int i = 0; i < limit; i++) {
            RequestItem item = items.get(i);
            int row = layout.startRow() + i;
            String description = buildItemDescriptionWithBizDate(item);
            if (TEMPLATE_CODE_A.equals(templateCode) || TEMPLATE_CODE_C.equals(templateCode)) {
                setCellText(sheet, row, layout.itemCol(), (i + 1) + ". " + description);
            } else {
                setCellText(sheet, row, layout.noCol(), String.valueOf(i + 1));
                setCellText(sheet, row, layout.brandCol(), safe(item.getBrandName()));
                setCellText(sheet, row, layout.itemCol(), description);
            }
            setCellNumber(sheet, row, layout.qtyCol(), item.getRequestQty() == null ? 0 : item.getRequestQty());
            setCellNumber(sheet, row, layout.unitPriceCol(), itemUnitPrice(item));
            setCellNumber(sheet, row, layout.amountCol(), itemAmount(item));
            setCellText(sheet, row, layout.remarkCol(), safe(item.getRemark()));
            if (layout.extraCol() >= 0) {
                setCellText(sheet, row, layout.extraCol(), safe(item.getSkuCode()));
            }
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

    private void setCellNumber(Sheet sheet, int rowIndex, int colIndex, BigDecimal value) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex, CellType.NUMERIC);
        } else {
            cell.setCellType(CellType.NUMERIC);
        }
        cell.setCellValue(value == null ? 0D : value.doubleValue());
    }

    private void setCellNumber(Sheet sheet, int rowIndex, int colIndex, int value) {
        setCellNumber(sheet, rowIndex, colIndex, BigDecimal.valueOf(value));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String customerName(RequestForm form, Customer customer) {
        if (customer != null && customer.getName() != null && !customer.getName().isBlank()) {
            return customer.getName();
        }
        return form == null ? "" : safe(form.getCustomerName());
    }

    private String contactLine(Customer customer) {
        if (customer == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        if (customer.getPhone() != null && !customer.getPhone().isBlank()) {
            parts.add("Tel: " + customer.getPhone());
        }
        if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
            parts.add("Email: " + customer.getEmail());
        }
        return String.join("  ", parts);
    }

    private String buildItemDescription(RequestItem item) {
        List<String> parts = new ArrayList<>();
        addPart(parts, item.getGoodsName());
        addPart(parts, item.getEnglishName());
        addPart(parts, item.getSkuCode());
        addPart(parts, item.getSeriesName());
        addPart(parts, item.getCategoryName());
        addPart(parts, item.getMakerName());
        addPart(parts, item.getStockTypeName());
        return String.join(" / ", parts);
    }

    private String buildItemDescriptionWithBizDate(RequestItem item) {
        String description = buildItemDescription(item);
        String bizDate = requestItemBizDateText(item);
        if (bizDate.isBlank()) {
            return description;
        }
        return description.isBlank() ? "出庫日: " + bizDate : description + " / 出庫日: " + bizDate;
    }

    private String requestItemBizDateText(RequestItem item) {
        if (item == null || item.getStockRecordId() == null) {
            return "";
        }
        StockRecord record = stockRecordService.getByIdNotDeleted(item.getStockRecordId());
        return record == null || record.getBizDate() == null ? "" : record.getBizDate().toString();
    }

    private void addPart(List<String> parts, String value) {
        if (value != null && !value.isBlank()) {
            parts.add(value);
        }
    }

    private BigDecimal itemUnitPrice(RequestItem item) {
        if (item.getDiscountPrice() != null) {
            return item.getDiscountPrice();
        }
        if (item.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = item.getDiscount() == null ? BigDecimal.ONE : item.getDiscount();
        return item.getPrice().multiply(discount).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal itemAmount(RequestItem item) {
        if (item.getTotalAmt() != null) {
            return item.getTotalAmt();
        }
        int qty = item.getRequestQty() == null ? 0 : item.getRequestQty();
        return itemUnitPrice(item).multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
    }

    private ItemLayout itemLayout(String templateCode) {
        if (TEMPLATE_CODE_B.equals(templateCode)) {
            return new ItemLayout(22, 499, 1, 8, 1, 2, 3, 4, 5, 6, 7, 8);
        }
        if (TEMPLATE_CODE_C.equals(templateCode)) {
            return new ItemLayout(17, 62, 1, 6, -1, -1, 1, 3, 4, 5, 6, -1);
        }
        return new ItemLayout(18, 29, 1, 12, -1, -1, 1, 9, 10, 11, 12, -1);
    }

    private record ItemLayout(
            int startRow,
            int endRow,
            int firstCol,
            int lastCol,
            int noCol,
            int brandCol,
            int itemCol,
            int qtyCol,
            int unitPriceCol,
            int amountCol,
            int remarkCol,
            int extraCol
    ) {
        private int capacity() {
            return endRow - startRow + 1;
        }
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
            Set<Long> explicitStockRecordIds = new HashSet<>();
            for (RequestFormItemBatchDTO.Item item : dto.getItems()) {
                for (Long stockRecordId : batchItemStockRecordIds(item)) {
                    explicitStockRecordIds.add(stockRecordId);
                }
            }
            for (RequestFormItemBatchDTO.Item item : dto.getItems()) {
                List<Long> stockRecordIds = batchItemStockRecordIds(item);
                if (item == null || stockRecordIds.isEmpty()) {
                    continue;
                }
                int qty = item.getRequestQty() == null ? 0 : item.getRequestQty();
                if (qty < 0) {
                    throw fail("requested qty must be >= 0");
                }
                if (qty > 0) {
                    mergeRequestedQuantities(quantities, stockRecordIds, qty);
                    for (Long handleStockRecordId : resolveMatchedHandleStockRecordIds(item)) {
                        if (!explicitStockRecordIds.contains(handleStockRecordId)) {
                            quantities.merge(handleStockRecordId, qty, Integer::sum);
                        }
                    }
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

    private void mergeRequestedQuantities(Map<Long, Integer> quantities, List<Long> stockRecordIds, int requestQty) {
        int remainingQty = requestQty;
        for (Long stockRecordId : stockRecordIds) {
            if (remainingQty <= 0) {
                break;
            }
            StockRecord record = requireAvailableOutboundRecord(stockRecordId);
            int availableQty = Math.max(availableOutboundQty(record), cartAddedQty(record));
            if (availableQty <= 0) {
                continue;
            }
            int allocatedQty = stockRecordIds.size() == 1 ? remainingQty : Math.min(remainingQty, availableQty);
            quantities.merge(stockRecordId, allocatedQty, Integer::sum);
            remainingQty -= allocatedQty;
        }
        if (remainingQty > 0) {
            throw fail("requested qty cannot exceed available outbound qty");
        }
    }

    private List<Long> batchItemStockRecordIds(RequestFormItemBatchDTO.Item item) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        if (item == null) {
            return List.of();
        }
        if (item.getStockRecordIds() != null) {
            for (Long id : item.getStockRecordIds()) {
                if (id != null && id > 0) {
                    ids.add(id);
                }
            }
        }
        if (item.getStockRecordId() != null && item.getStockRecordId() > 0) {
            ids.add(item.getStockRecordId());
        }
        return new ArrayList<>(ids);
    }

    private List<Long> resolveMatchedHandleStockRecordIds(RequestFormItemBatchDTO.Item item) {
        java.util.LinkedHashSet<Long> ids = new java.util.LinkedHashSet<>();
        if (item.getHandleStockRecordId() != null && item.getHandleStockRecordId() > 0) {
            ids.add(item.getHandleStockRecordId());
        }
        if (item.getHandleStockRecordIds() != null) {
            for (Long id : item.getHandleStockRecordIds()) {
                if (id != null && id > 0) {
                    ids.add(id);
                }
            }
        }
        return new ArrayList<>(ids);
    }

    private StockOrder requireOwnedSourceOutbound(RequestForm form) {
        if (form.getSourceOrderId() == null) {
            throw fail("request form source outbound order is required");
        }
        StockOrder order = stockOrderService.getByIdNotDeleted(form.getSourceOrderId());
        if (order == null || !Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(order.getOrderType())) {
            throw fail("source outbound order not found");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_STATE_FINISHED).equals(order.getState())) {
            throw fail("source outbound order is not finished");
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (permissionQueryService.isSuperAdmin(userId)) {
            return order;
        }
        if (!userId.equals(order.getRequesterId()) && !userId.equals(order.getOperatorId())) {
            throw fail("source outbound order is not owned by current user");
        }
        return order;
    }

    private StockRecord requireAvailableOutboundRecord(Long stockRecordId) {
        StockRecord record = stockRecordService.getByIdNotDeleted(stockRecordId);
        if (record == null || !Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(record.getOrderType())) {
            throw fail("selected stock record is not available");
        }
        StockOrder order = stockOrderService.getByIdNotDeleted(record.getOrderId());
        if (order == null || !Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(order.getOrderType())
                || !Integer.valueOf(StockBizConstant.ORDER_STATE_FINISHED).equals(order.getState())) {
            throw fail("source outbound order is not finished");
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId)
                && !userId.equals(record.getRequesterId())
                && !userId.equals(record.getOperatorId())
                && !canAccessCustomerRecord(record, userId)) {
            throw fail("source outbound order is not owned by current user");
        }
        return record;
    }

    private boolean canAccessCustomerRecord(StockRecord record, Long userId) {
        if (record == null) {
            return false;
        }
        if (permissionQueryService.isSuperAdmin(userId)) {
            return true;
        }
        if (record.getCustomerId() == null || userId == null) {
            return false;
        }
        Customer customer = customerService.getByIdNotDeleted(record.getCustomerId());
        return customer != null && userId.equals(customer.getOwnerUserId());
    }

    private void applyOutboundRemainderDelta(StockRecord record, int deltaQty) {
        if (deltaQty == 0) {
            return;
        }
        StockOrderItem orderItem = stockOrderItemService.getByIdNotDeleted(record.getOrderItemId());
        if (orderItem == null) {
            throw fail("source outbound item not found");
        }
        int originalQty = record.getChangeQty() == null ? 0 : Math.abs(record.getChangeQty());
        Integer currentRaw = orderItem.getChangeQty();
        int currentQty = currentRaw == null ? 0 : Math.abs(currentRaw);
        int nextQty = currentQty - deltaQty;
        if (nextQty < 0 || nextQty > originalQty) {
            throw fail("requested qty cannot exceed available outbound qty");
        }
        int affected = stockOrderItemService.getBaseMapper().update(
                null,
                new LambdaUpdateWrapper<StockOrderItem>()
                        .eq(StockOrderItem::getId, orderItem.getId())
                        .eq(StockOrderItem::getChangeQty, currentRaw)
                        .set(StockOrderItem::getChangeQty, nextQty)
        );
        if (affected <= 0) {
            throw fail("source outbound item changed concurrently, please retry");
        }
        // Cart moves only change the remaining delivery-schedule qty of the item.
        // The source outbound order total is historical and must stay immutable.
    }

    private void applyOutboundRemainderByRequested(StockRecord record, int requestedQty, int currentRequestQty) {
        StockOrderItem orderItem = stockOrderItemService.getByIdNotDeleted(record.getOrderItemId());
        if (orderItem == null) {
            throw fail("source outbound item not found");
        }
        int originalQty = record.getChangeQty() == null ? 0 : Math.abs(record.getChangeQty());
        Integer currentRaw = orderItem.getChangeQty();
        int currentRemainingQty = currentRaw == null ? 0 : Math.abs(currentRaw);

        int reflectedCurrentRequestQty = Math.min(
                Math.max(0, currentRequestQty),
                Math.max(0, originalQty - currentRemainingQty)
        );
        int maxRequestQty = currentRemainingQty + reflectedCurrentRequestQty;
        if (maxRequestQty > originalQty) {
            maxRequestQty = originalQty;
        }
        if (requestedQty < 0 || requestedQty > maxRequestQty) {
            throw fail("requested qty cannot exceed available outbound qty"
                    + ", stockRecordId=" + record.getId()
                    + ", available=" + maxRequestQty
                    + ", requested=" + requestedQty);
        }

        int targetRemainingQty = currentRemainingQty + reflectedCurrentRequestQty - requestedQty;
        if (targetRemainingQty < 0 || targetRemainingQty > originalQty) {
            throw fail("requested qty cannot exceed available outbound qty"
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
                        .eq(StockOrderItem::getChangeQty, currentRaw)
                        .set(StockOrderItem::getChangeQty, targetRemainingQty)
        );
        if (affected <= 0) {
            throw fail("source outbound item changed concurrently, please retry");
        }

        validateCartMoveBalance(record);
        // Keep t_stock_order.total_qty as the approved outbound total.
    }

    private void validateCartMoveBalance(StockRecord record) {
        if (record == null) {
            return;
        }
        StockOrderItem orderItem = stockOrderItemService.getByIdNotDeleted(record.getOrderItemId());
        if (orderItem == null) {
            throw fail("source outbound item not found");
        }
        int originalQty = record.getChangeQty() == null ? 0 : Math.abs(record.getChangeQty());
        int remainingQty = orderItem.getChangeQty() == null ? 0 : Math.abs(orderItem.getChangeQty());
        if (remainingQty > originalQty) {
            throw fail("delivery schedule qty exceeds source outbound qty"
                    + ", stockRecordId=" + record.getId()
                    + ", source=" + originalQty
                    + ", remaining=" + remainingQty);
        }
        int cartQty = originalQty - remainingQty;
        if (remainingQty + cartQty != originalQty) {
            throw fail("delivery schedule/request item qty is inconsistent"
                    + ", stockRecordId=" + record.getId()
                    + ", source=" + originalQty
                    + ", remaining=" + remainingQty
                    + ", requestItem=" + cartQty);
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
                    .last("LIMIT 1"));
            int requestQty = requestItem == null || requestItem.getRequestQty() == null ? 0 : Math.abs(requestItem.getRequestQty());
            if (requestQty + remainingQty != originalQty) {
                throw fail("request and outbound quantities are inconsistent");
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
                    .last("LIMIT 1"));
            if (existed == null) {
                throw ex;
            }
            existed.setState(StockBizConstant.REQUEST_ITEM_STATE_ADDED);
            existed.setRequestQty(candidate.getRequestQty());
            existed.setOutQty(candidate.getOutQty());
            existed.setPrice(candidate.getPrice());
            existed.setDiscountPrice(candidate.getDiscountPrice());
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
        requestItem.setWarehouseId(record.getWarehouseId());
        requestItem.setPrice(record.getPrice());
        requestItem.setDiscountPrice(record.getPrice());
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
            throw fail("request form not found");
        }
        List<RequestItem> items = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", requestId)
                .eq("state", StockBizConstant.REQUEST_ITEM_STATE_ADDED)
                );
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
            throw fail("failed to update request form");
        }
    }

    private List<StockRecord> listSourceOutboundRecords(RequestForm form) {
        requireOwnedSourceOutbound(form);
        return stockRecordService.list(new QueryWrapper<StockRecord>()
                .eq("order_id", form.getSourceOrderId())
                .eq("order_type", StockBizConstant.ORDER_TYPE_OUTBOUND));
    }

    private List<StockRecord> listAvailableOutboundRecords() {
        Long userId = UserContext.getUserIdOrDefault();
        QueryWrapper<StockRecord> wrapper = new QueryWrapper<StockRecord>()
                .eq("order_type", StockBizConstant.ORDER_TYPE_OUTBOUND)
                .orderByDesc("biz_date", "id");
        if (!permissionQueryService.isSuperAdmin(userId)) {
            wrapper.and(query -> query.eq("requester_id", userId).or().eq("operator_id", userId));
        }
        List<StockRecord> records = stockRecordService.list(wrapper);
        if (records.isEmpty()) {
            return records;
        }

        Set<Long> orderIds = new HashSet<>();
        for (StockRecord record : records) {
            if (record.getOrderId() != null) {
                orderIds.add(record.getOrderId());
            }
        }
        if (orderIds.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> finishedOrderIds = new HashSet<>();
        for (StockOrder order : stockOrderService.list(new QueryWrapper<StockOrder>()
                .in("id", orderIds)
                .eq("order_type", StockBizConstant.ORDER_TYPE_OUTBOUND)
                .eq("state", StockBizConstant.ORDER_STATE_FINISHED))) {
            finishedOrderIds.add(order.getId());
        }
        return records.stream()
                .filter(record -> finishedOrderIds.contains(record.getOrderId()))
                .toList();
    }

    private void validateKnifeHandleQuantity(Long requestId) {
        List<RequestItem> items = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", requestId)
                .eq("state", StockBizConstant.REQUEST_ITEM_STATE_ADDED)
                );
        RequestForm form = this.getByIdNotDeleted(requestId);
        if (form == null || !sourceHasKnifeAndHandle(form)) {
            return;
        }
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
        if (knifeQty != handleQty) {
            throw fail("knife and handle quantities must match");
        }
    }

    private boolean isKnifeItem(RequestItem item) {
        return isKnifeCategory(item.getCategoryName());
    }

    private boolean isHandleItem(RequestItem item) {
        return isHandleCategory(item.getCategoryName());
    }

    private boolean sourceHasKnifeAndHandle(RequestForm form) {
        boolean hasKnife = false;
        boolean hasHandle = false;
        List<RequestItem> items = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", form.getId())
                .eq("state", StockBizConstant.REQUEST_ITEM_STATE_ADDED)
                );
        for (RequestItem item : items) {
            hasKnife = hasKnife || isKnifeCategory(item.getCategoryName());
            hasHandle = hasHandle || isHandleCategory(item.getCategoryName());
        }
        return hasKnife && hasHandle;
    }

    private boolean isKnifeCategory(String categoryName) {
        return containsAnyConfiguredKeyword(categoryName, CONFIG_KNIFE_CATEGORY_KEYWORDS, List.of("厨刀", "刀"));
    }

    private boolean isHandleCategory(String categoryName) {
        return containsAnyConfiguredKeyword(categoryName, CONFIG_HANDLE_CATEGORY_KEYWORDS, List.of("ハンドル", "柄"));
    }

    private boolean containsAnyConfiguredKeyword(String value, String configName, List<String> defaults) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (String keyword : getConfiguredKeywords(configName, defaults)) {
            if (keyword != null && !keyword.isBlank() && value.contains(keyword.trim())) {
                return true;
            }
        }
        return false;
    }

    private List<String> getConfiguredKeywords(String configName, List<String> defaults) {
        String value = getConfigValueByName(configName);
        if (value == null || value.isBlank()) {
            return defaults;
        }
        List<String> keywords = new ArrayList<>();
        for (String keyword : value.split("[,，\\n\\r]+")) {
            String normalized = keyword.trim();
            if (!normalized.isBlank()) {
                keywords.add(normalized);
            }
        }
        return keywords.isEmpty() ? defaults : keywords;
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
        return stockMapper.selectOne(wrapper);
    }

    private StockOrder findDeliveryInboundOrder(Long stockRecordId) {
        if (stockRecordId == null) {
            return null;
        }
        return stockOrderService.getOne(new QueryWrapper<StockOrder>()
                .eq("order_type", StockBizConstant.ORDER_TYPE_INBOUND)
                .eq("source_type", StockBizConstant.SOURCE_TYPE_REQUEST)
                .eq("source_id", stockRecordId)
                .likeRight("remark", REMARK_DELIVERY_INBOUND)
                .last("LIMIT 1"));
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
            throw fail("customer is not owned by current user");
        }
    }

    private void applyCurrentUser(CreateRequestFormDTO dto, Long userId) {
        dto.setUserId(userId);
        User user = userService.getByIdNotDeleted(userId);
        dto.setUsername(user == null ? null : user.getUsername());
        dto.setDeptId(user == null ? null : user.getDeptId());
        Dept dept = user != null && user.getDeptId() != null ? deptService.getByIdNotDeleted(user.getDeptId()) : null;
        dto.setDeptName(dept == null ? null : dept.getName());
    }

    private void applyRequestFormCreateDefaults(CreateRequestFormDTO dto) {
        if (dto.getSourceOrderId() != null) {
            StockOrder sourceOrder = requireCreateSourceOutbound(dto.getSourceOrderId());
            dto.setWarehouseId(sourceOrder.getWarehouseId());
        }
        dto.setTotalQty(0);
        dto.setRequestQty(0);
        dto.setTotalAmt(BigDecimal.ZERO);
        dto.setState(StockBizConstant.REQUEST_STATE_DRAFT);
        dto.setApproverId(null);
        dto.setApproverName(null);
        dto.setApproveTime(null);
        applyCustomerName(dto);
    }

    private StockOrder requireCreateSourceOutbound(Long sourceOrderId) {
        if (sourceOrderId == null) {
            throw fail("request form source outbound order is required");
        }
        StockOrder order = stockOrderService.getByIdNotDeleted(sourceOrderId);
        if (order == null || !Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(order.getOrderType())) {
            throw fail("source outbound order not found");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_STATE_FINISHED).equals(order.getState())) {
            throw fail("source outbound order is not finished");
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId)
                && !userId.equals(order.getRequesterId())
                && !userId.equals(order.getOperatorId())) {
            throw fail("source outbound order is not owned by current user");
        }
        return order;
    }

    private void applyCustomerName(CreateRequestFormDTO dto) {
        Customer customer = customerService.getByIdNotDeleted(dto.getCustomerId());
        if (customer == null) {
            throw fail("customer is not owned by current user");
        }
        dto.setCustomerName(customer.getName());
    }

    private void applyCustomerName(UpdateRequestFormDTO dto) {
        Customer customer = customerService.getByIdNotDeleted(dto.getCustomerId());
        if (customer == null) {
            throw fail("customer is not owned by current user");
        }
        dto.setCustomerName(customer.getName());
    }

    private void preserveRequestFormBackendFields(UpdateRequestFormDTO dto, RequestForm existed) {
        dto.setUserId(existed.getUserId());
        dto.setUsername(existed.getUsername());
        dto.setDeptId(existed.getDeptId());
        dto.setDeptName(existed.getDeptName());
        dto.setWarehouseId(existed.getWarehouseId());
        dto.setSourceOrderId(existed.getSourceOrderId());
        dto.setTotalQty(existed.getTotalQty());
        dto.setRequestQty(existed.getRequestQty());
        dto.setTotalAmt(existed.getTotalAmt());
    }

    private void requireOwned(RequestForm form) {
        if (form == null) {
            return;
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId) && !userId.equals(form.getUserId())) {
            throw fail("request form is not owned by current user");
        }
    }

    private void requireRequestFormEditable(RequestForm form) {
        if (form == null) {
            return;
        }
        if (isRequestFormCompleted(form.getState())) {
            throw fail("request form is completed and cannot be modified");
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId)
                && !Integer.valueOf(StockBizConstant.REQUEST_STATE_DRAFT).equals(form.getState())
                && !Integer.valueOf(StockBizConstant.REQUEST_STATE_SUBMITTED).equals(form.getState())) {
            throw fail("request form state is not editable for current user");
        }
    }

    private void validateRequestFormState(Integer state, Long userId) {
        if (state == null) {
            return;
        }
        boolean admin = permissionQueryService.isSuperAdmin(userId);
        boolean allowed = admin
                ? state >= StockBizConstant.REQUEST_STATE_DRAFT && state <= StockBizConstant.REQUEST_STATE_CANCELED
                : state.equals(StockBizConstant.REQUEST_STATE_DRAFT)
                || state.equals(StockBizConstant.REQUEST_STATE_SUBMITTED);
        if (!allowed) {
            throw fail("request form state is not allowed for current user");
        }
    }

    private boolean isRequestFormCompleted(Integer state) {
        return Integer.valueOf(StockBizConstant.REQUEST_STATE_FINISHED).equals(state);
    }

    private BusinessException fail(String message) {
        return new BusinessException(MessageKeyConstant.ERROR_RUNTIME, withErrorCode(message));
    }

    private BusinessException fail(String message, Throwable cause) {
        return new BusinessException(MessageKeyConstant.ERROR_RUNTIME, withErrorCode(message), cause);
    }

    private String withErrorCode(String message) {
        String code = resolveErrorCode(message);
        return "[" + code + "] " + resolveErrorMessage(message);
    }

    private String resolveErrorCode(String message) {
        if (message == null || message.isBlank()) {
            return DEFAULT_ERROR_CODE;
        }
        for (Map.Entry<String, String> entry : ERROR_CODE_BY_MESSAGE.entrySet()) {
            String knownMessage = entry.getKey();
            if (message.equals(knownMessage) || message.startsWith(knownMessage + ",")) {
                return entry.getValue();
            }
        }
        return DEFAULT_ERROR_CODE;
    }

    private String resolveErrorMessage(String message) {
        if (message == null || message.isBlank()) {
            return "請求書処理に失敗しました";
        }
        for (Map.Entry<String, String> entry : ERROR_MESSAGE_JA_BY_MESSAGE.entrySet()) {
            String knownMessage = entry.getKey();
            if (message.equals(knownMessage)) {
                return entry.getValue();
            }
            if (message.startsWith(knownMessage + ",")) {
                return entry.getValue() + message.substring(knownMessage.length());
            }
        }
        return message;
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
