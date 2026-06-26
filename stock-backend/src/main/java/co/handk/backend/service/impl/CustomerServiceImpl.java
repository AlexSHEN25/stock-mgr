package co.handk.backend.service.impl;

import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.entity.Customer;
import co.handk.backend.entity.CustomerLevel;
import co.handk.backend.entity.Dept;
import co.handk.backend.entity.User;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.mapper.CustomerMapper;
import co.handk.backend.service.CustomerLevelService;
import co.handk.backend.service.CustomerService;
import co.handk.backend.service.DeptService;
import co.handk.backend.service.PermissionQueryService;
import co.handk.backend.service.UserService;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.dto.create.CreateCustomerDTO;
import co.handk.common.model.dto.customer.CustomerImportItemDTO;
import co.handk.common.model.dto.query.CustomerQueryDTO;
import co.handk.common.model.dto.update.UpdateCustomerDTO;
import co.handk.common.model.vo.CustomerImportResultVO;
import co.handk.common.model.vo.CustomerImportRowResultVO;
import co.handk.common.model.vo.CustomerVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl extends BaseServiceImpl<CustomerMapper, Customer, CustomerVO>
        implements CustomerService {

    private static final String CUSTOMER_EXPORT_FILE_NAME = "顧客一覧.xlsx";
    private static final String CUSTOMER_IMPORT_TEMPLATE_FILE_NAME = "顧客一括取込テンプレート.xlsx";
    private static final String IMPORT_SHEET_NAME = "顧客";
    private static final String VALIDATION_SHEET_NAME = "_customer_validation";
    private static final int HEADER_ROW_INDEX = 0;
    private static final int DATA_START_ROW_INDEX = 1;
    private static final int VALIDATION_MAX_ROW = 5000;
    private static final int EXCEL_COLUMN_WIDTH = 18 * 256;
    private static final long EXPORT_MAX_ROWS = 10_000L;
    private static final String ACTION_CREATED = "CREATED";
    private static final String ACTION_UPDATED = "UPDATED";
    private static final String ACTION_FAILED = "FAILED";
    private static final String ERROR_EXPORT_FAILED = "顧客Excelの出力に失敗しました";
    private static final String ERROR_IMPORT_READ_FAILED = "顧客取込ファイルの読み取りに失敗しました";
    private static final List<String> IMPORT_HEADERS = List.of(
            "ID",
            "顧客コード",
            "顧客名",
            "英語名",
            "担当者",
            "電話番号",
            "メールアドレス",
            "国",
            "都市",
            "住所",
            "顧客ランク",
            "担当ユーザー",
            "担当部署",
            "備考",
            "状態"
    );

    private final PermissionQueryService permissionQueryService;
    private final CustomerLevelService customerLevelService;
    private final UserService userService;
    private final DeptService deptService;

    @Override
    protected CustomerVO toVO(Customer entity) {
        if (entity == null) {
            return null;
        }
        CustomerVO vo = new CustomerVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> Customer toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Customer entity = new Customer();
        BeanUtils.copyProperties(dto, entity);
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId)) {
            entity.setOwnerUserId(userId);
        }
        return entity;
    }

    @Override
    protected <Q> QueryWrapper<Customer> buildWrapper(Q dto) {
        QueryWrapper<Customer> wrapper = super.buildWrapper(dto);
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId)) {
            wrapper.eq("owner_user_id", userId);
        }
        return wrapper;
    }

    @Override
    public Customer getByIdNotDeleted(Serializable id) {
        Customer customer = super.getByIdNotDeleted(id);
        requireOwned(customer);
        return customer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        Customer entity = toEntity(dto);
        if (entity == null || entity.getId() == null) {
            return super.updateByDto(dto);
        }
        Customer existed = super.getByIdNotDeleted(entity.getId());
        requireOwned(existed);
        return super.updateByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIdLogic(Long id) {
        Customer customer = super.getByIdNotDeleted(id);
        requireOwned(customer);
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
            Customer customer = super.getByIdNotDeleted(id);
            requireOwned(customer);
            rows += super.deleteByIdLogic(id);
        }
        return rows;
    }

    @Override
    public void exportCustomers(CustomerQueryDTO query, HttpServletResponse response) {
        CustomerQueryDTO exportQuery = new CustomerQueryDTO();
        if (query != null) {
            BeanUtils.copyProperties(query, exportQuery);
        }
        exportQuery.setPageNum(1L);
        exportQuery.setPageSize(EXPORT_MAX_ROWS);
        List<CustomerVO> records = page(exportQuery).getRecords();
        try (XSSFWorkbook workbook = buildCustomerWorkbook(records, false)) {
            writeWorkbookResponse(workbook, response, CUSTOMER_EXPORT_FILE_NAME);
        } catch (IOException ex) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_EXPORT_FAILED, ex);
        }
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) {
        try (XSSFWorkbook workbook = buildCustomerWorkbook(List.of(), true)) {
            writeWorkbookResponse(workbook, response, CUSTOMER_IMPORT_TEMPLATE_FILE_NAME);
        } catch (IOException ex) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_EXPORT_FAILED, ex);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerImportResultVO importCustomers(MultipartFile file) {
        List<CustomerImportItemDTO> items = parseImportFile(file);
        CustomerImportResultVO result = new CustomerImportResultVO();
        Map<Long, Integer> idRows = new HashMap<>();
        Map<String, Integer> codeRows = new HashMap<>();
        for (CustomerImportItemDTO item : items) {
            try {
                validateImportIdentityUniqueness(item, idRows, codeRows);
                CustomerImportRowResultVO rowResult = upsertImportedCustomer(item);
                result.getRows().add(rowResult);
                result.setSuccessCount(result.getSuccessCount() + 1);
                if (ACTION_CREATED.equals(rowResult.getAction())) {
                    result.setCreatedCount(result.getCreatedCount() + 1);
                } else if (ACTION_UPDATED.equals(rowResult.getAction())) {
                    result.setUpdatedCount(result.getUpdatedCount() + 1);
                }
            } catch (Exception ex) {
                result.getRows().add(buildFailedRowResult(item, resolveExceptionMessage(ex)));
                result.setFailureCount(result.getFailureCount() + 1);
            }
        }
        return result;
    }

    private void requireOwned(Customer customer) {
        if (customer == null) {
            return;
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (permissionQueryService.isSuperAdmin(userId)) {
            return;
        }
        if (!userId.equals(customer.getOwnerUserId())) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "顧客の担当情報が一致しません");
        }
    }

    private XSSFWorkbook buildCustomerWorkbook(List<CustomerVO> records, boolean templateOnly) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(IMPORT_SHEET_NAME);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        var headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(HEADER_ROW_INDEX);
        for (int i = 0; i < IMPORT_HEADERS.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(IMPORT_HEADERS.get(i));
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, EXCEL_COLUMN_WIDTH);
        }
        if (!templateOnly) {
            int rowIndex = DATA_START_ROW_INDEX;
            for (CustomerVO record : records) {
                Row row = sheet.createRow(rowIndex++);
                writeCell(row, 0, record.getId());
                writeCell(row, 1, record.getCustomerCode());
                writeCell(row, 2, record.getName());
                writeCell(row, 3, record.getEnglishName());
                writeCell(row, 4, record.getContactPerson());
                writeCell(row, 5, record.getPhone());
                writeCell(row, 6, record.getEmail());
                writeCell(row, 7, record.getCountry());
                writeCell(row, 8, record.getCity());
                writeCell(row, 9, record.getAddress());
                writeCell(row, 10, record.getLevelName());
                writeCell(row, 11, record.getOwnerUserName());
                writeCell(row, 12, record.getOwnerDeptName());
                writeCell(row, 13, record.getRemark());
                writeCell(row, 14, renderStatus(record.getStatus()));
            }
        }
        buildCustomerValidationSheet(workbook);
        applyCustomerTemplateValidations(workbook);
        return workbook;
    }

    private void buildCustomerValidationSheet(XSSFWorkbook workbook) {
        Sheet sheet = workbook.createSheet(VALIDATION_SHEET_NAME);
        int rowIndex = 0;
        rowIndex = writeValidationBlock(sheet, rowIndex, "CUSTOMER_LEVEL_NAME",
                customerLevelService.list(new QueryWrapper<CustomerLevel>().orderByAsc("id"))
                        .stream()
                        .map(CustomerLevel::getName)
                        .toList());
        rowIndex = writeValidationBlock(sheet, rowIndex, "OWNER_USER_NAME",
                userService.list(new QueryWrapper<User>().orderByAsc("id"))
                        .stream()
                        .map(User::getUsername)
                        .toList());
        rowIndex = writeValidationBlock(sheet, rowIndex, "OWNER_DEPT_NAME",
                deptService.list(new QueryWrapper<Dept>().orderByAsc("id"))
                        .stream()
                        .map(Dept::getName)
                        .toList());
        writeValidationBlock(sheet, rowIndex, "STATUS_NAME", List.of("有効", "無効"));
        workbook.setSheetHidden(workbook.getSheetIndex(sheet), true);
    }

    private int writeValidationBlock(Sheet sheet, int rowIndex, String rangeName, List<String> values) {
        Row titleRow = sheet.createRow(rowIndex);
        titleRow.createCell(0).setCellValue(rangeName);
        int startRow = rowIndex + 1;
        int currentRow = startRow;
        for (String value : values) {
            String normalized = trimToNull(value);
            if (normalized == null) {
                continue;
            }
            Row row = sheet.createRow(currentRow++);
            row.createCell(0).setCellValue(normalized);
        }
        if (currentRow == startRow) {
            Row row = sheet.createRow(currentRow++);
            row.createCell(0).setCellValue("__EMPTY__");
        }
        Name name = sheet.getWorkbook().createName();
        name.setNameName(rangeName);
        name.setRefersToFormula("'" + VALIDATION_SHEET_NAME + "'!$A$" + startRow + ":$A$" + (currentRow - 1));
        return currentRow + 1;
    }

    private void applyCustomerTemplateValidations(XSSFWorkbook workbook) {
        Sheet sheet = workbook.getSheet(IMPORT_SHEET_NAME);
        if (sheet == null) {
            return;
        }
        addExplicitListValidation(sheet, DATA_START_ROW_INDEX, VALIDATION_MAX_ROW, 10, "CUSTOMER_LEVEL_NAME");
        addExplicitListValidation(sheet, DATA_START_ROW_INDEX, VALIDATION_MAX_ROW, 11, "OWNER_USER_NAME");
        addExplicitListValidation(sheet, DATA_START_ROW_INDEX, VALIDATION_MAX_ROW, 12, "OWNER_DEPT_NAME");
        addExplicitListValidation(sheet, DATA_START_ROW_INDEX, VALIDATION_MAX_ROW, 14, "STATUS_NAME");
    }

    private void addExplicitListValidation(Sheet sheet, int firstRow, int lastRow, int columnIndex, String namedRange) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createFormulaListConstraint(namedRange);
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, columnIndex, columnIndex);
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(false);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    private List<CustomerImportItemDTO> parseImportFile(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            return parseImportWorkbook(workbook);
        } catch (Exception ex) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_IMPORT_READ_FAILED, ex);
        }
    }

    private List<CustomerImportItemDTO> parseImportWorkbook(Workbook workbook) {
        Sheet sheet = workbook.getSheet(IMPORT_SHEET_NAME);
        if (sheet == null) {
            sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
        }
        if (sheet == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "顧客取込シートが見つかりません");
        }
        Row headerRow = sheet.getRow(HEADER_ROW_INDEX);
        if (headerRow == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "顧客取込テンプレートのヘッダー行が見つかりません");
        }
        DataFormatter formatter = new DataFormatter();
        Map<String, Integer> headerIndexes = resolveHeaderIndexes(headerRow, formatter);
        List<CustomerImportItemDTO> items = new ArrayList<>();
        for (int rowIndex = DATA_START_ROW_INDEX; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null || isRowEmpty(row, formatter)) {
                continue;
            }
            CustomerImportItemDTO item = new CustomerImportItemDTO();
            item.setRowNo(rowIndex + 1);
            item.setId(readLong(row, headerIndexes.get("ID"), formatter));
            item.setCustomerCode(readString(row, headerIndexes.get("顧客コード"), formatter));
            item.setName(readString(row, headerIndexes.get("顧客名"), formatter));
            item.setEnglishName(readString(row, headerIndexes.get("英語名"), formatter));
            item.setContactPerson(readString(row, headerIndexes.get("担当者"), formatter));
            item.setPhone(readString(row, headerIndexes.get("電話番号"), formatter));
            item.setEmail(readString(row, headerIndexes.get("メールアドレス"), formatter));
            item.setCountry(readString(row, headerIndexes.get("国"), formatter));
            item.setCity(readString(row, headerIndexes.get("都市"), formatter));
            item.setAddress(readString(row, headerIndexes.get("住所"), formatter));
            item.setLevelName(readString(row, headerIndexes.get("顧客ランク"), formatter));
            item.setOwnerUserName(readString(row, headerIndexes.get("担当ユーザー"), formatter));
            item.setOwnerDeptName(readString(row, headerIndexes.get("担当部署"), formatter));
            item.setRemark(readString(row, headerIndexes.get("備考"), formatter));
            item.setStatus(readString(row, headerIndexes.get("状態"), formatter));
            items.add(item);
        }
        return items;
    }

    private CustomerImportRowResultVO upsertImportedCustomer(CustomerImportItemDTO item) {
        validateImportRow(item);
        Customer existing = resolveExistingCustomer(item);
        if (existing == null) {
            CreateCustomerDTO dto = new CreateCustomerDTO();
            dto.setCustomerCode(trimToNull(item.getCustomerCode()));
            dto.setName(trimToNull(item.getName()));
            dto.setEnglishName(trimToNull(item.getEnglishName()));
            dto.setContactPerson(trimToNull(item.getContactPerson()));
            dto.setPhone(trimToNull(item.getPhone()));
            dto.setEmail(trimToNull(item.getEmail()));
            dto.setCountry(trimToNull(item.getCountry()));
            dto.setCity(trimToNull(item.getCity()));
            dto.setAddress(trimToNull(item.getAddress()));
            dto.setLevelId(resolveCustomerLevelId(item));
            dto.setOwnerUserId(resolveOwnerUserId(item));
            dto.setOwnerDeptId(resolveOwnerDeptId(item));
            dto.setRemark(trimToNull(item.getRemark()));
            dto.setStatus(parseStatus(item.getStatus()));
            if (!saveByDto(dto)) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "顧客の登録に失敗しました");
            }
            Customer created = trimToNull(item.getCustomerCode()) == null
                    ? null
                    : findAccessibleByCustomerCode(item.getCustomerCode());
            CustomerImportRowResultVO rowResult = new CustomerImportRowResultVO();
            rowResult.setRowNo(item.getRowNo());
            rowResult.setSuccess(true);
            rowResult.setAction(ACTION_CREATED);
            rowResult.setCustomerId(created == null ? null : created.getId());
            rowResult.setCustomerCode(item.getCustomerCode());
            rowResult.setMessage("登録しました");
            return rowResult;
        }
        UpdateCustomerDTO dto = new UpdateCustomerDTO();
        dto.setId(existing.getId());
        dto.setCustomerCode(firstNonBlank(item.getCustomerCode(), existing.getCustomerCode()));
        dto.setName(firstNonBlank(item.getName(), existing.getName()));
        dto.setEnglishName(firstNonBlank(item.getEnglishName(), existing.getEnglishName()));
        dto.setContactPerson(firstNonBlank(item.getContactPerson(), existing.getContactPerson()));
        dto.setPhone(firstNonBlank(item.getPhone(), existing.getPhone()));
        dto.setEmail(firstNonBlank(item.getEmail(), existing.getEmail()));
        dto.setCountry(firstNonBlank(item.getCountry(), existing.getCountry()));
        dto.setCity(firstNonBlank(item.getCity(), existing.getCity()));
        dto.setAddress(firstNonBlank(item.getAddress(), existing.getAddress()));
        dto.setLevelId(firstNonNull(resolveCustomerLevelId(item), existing.getLevelId()));
        dto.setOwnerUserId(firstNonNull(resolveOwnerUserId(item), existing.getOwnerUserId()));
        dto.setOwnerDeptId(firstNonNull(resolveOwnerDeptId(item), existing.getOwnerDeptId()));
        dto.setRemark(firstNonBlank(item.getRemark(), existing.getRemark()));
        dto.setStatus(parseStatus(item.getStatus(), existing.getStatus()));
        if (!updateByDto(dto)) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "顧客の更新に失敗しました");
        }
        CustomerImportRowResultVO rowResult = new CustomerImportRowResultVO();
        rowResult.setRowNo(item.getRowNo());
        rowResult.setSuccess(true);
        rowResult.setAction(ACTION_UPDATED);
        rowResult.setCustomerId(existing.getId());
        rowResult.setCustomerCode(dto.getCustomerCode());
        rowResult.setMessage("更新しました");
        return rowResult;
    }

    private Integer resolveCustomerLevelId(CustomerImportItemDTO item) {
        String name = trimToNull(item.getLevelName());
        if (name == null) {
            return null;
        }
        CustomerLevel level = customerLevelService.getOne(new QueryWrapper<CustomerLevel>()
                .eq("name", name)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
        if (level == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, "顧客ランクが見つかりません: " + name));
        }
        return level.getId() == null ? null : level.getId().intValue();
    }

    private Long resolveOwnerUserId(CustomerImportItemDTO item) {
        String username = trimToNull(item.getOwnerUserName());
        if (username == null) {
            return null;
        }
        User user = userService.getOne(new QueryWrapper<User>()
                .eq("username", username)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
        if (user == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, "担当ユーザーが見つかりません: " + username));
        }
        return user.getId();
    }

    private Long resolveOwnerDeptId(CustomerImportItemDTO item) {
        String name = trimToNull(item.getOwnerDeptName());
        if (name == null) {
            return null;
        }
        Dept dept = deptService.getOne(new QueryWrapper<Dept>()
                .eq("name", name)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
        if (dept == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, "担当部署が見つかりません: " + name));
        }
        return dept.getId();
    }

    private void validateImportIdentityUniqueness(CustomerImportItemDTO item,
                                                  Map<Long, Integer> idRows,
                                                  Map<String, Integer> codeRows) {
        if (item.getId() != null) {
            Integer existingRow = idRows.putIfAbsent(item.getId(), item.getRowNo());
            if (existingRow != null) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                        rowMessage(item, "取込ファイル内でIDが重複しています。最初の行: " + existingRow));
            }
        }
        String code = trimToNull(item.getCustomerCode());
        if (code != null) {
            Integer existingRow = codeRows.putIfAbsent(code, item.getRowNo());
            if (existingRow != null) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                        rowMessage(item, "取込ファイル内で顧客コードが重複しています。最初の行: " + existingRow));
            }
        }
    }

    private void validateImportRow(CustomerImportItemDTO item) {
        if (item.getId() == null && trimToNull(item.getCustomerCode()) == null && trimToNull(item.getName()) == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, "顧客名または顧客コードを入力してください"));
        }
        if (item.getId() == null && trimToNull(item.getName()) == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, "新規登録時は顧客名を入力してください"));
        }
    }

    private Customer resolveExistingCustomer(CustomerImportItemDTO item) {
        if (item.getId() != null) {
            Customer byId = super.getByIdNotDeleted(item.getId());
            requireOwned(byId);
            return byId;
        }
        String code = trimToNull(item.getCustomerCode());
        if (code == null) {
            return null;
        }
        return findAccessibleByCustomerCode(code);
    }

    private Customer findAccessibleByCustomerCode(String customerCode) {
        QueryWrapper<Customer> wrapper = new QueryWrapper<Customer>()
                .eq("customer_code", customerCode.trim())
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1");
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId)) {
            wrapper.eq("owner_user_id", userId);
        }
        return this.getOne(wrapper);
    }

    private CustomerImportRowResultVO buildFailedRowResult(CustomerImportItemDTO item, String message) {
        CustomerImportRowResultVO rowResult = new CustomerImportRowResultVO();
        rowResult.setRowNo(item.getRowNo());
        rowResult.setSuccess(false);
        rowResult.setAction(ACTION_FAILED);
        rowResult.setCustomerId(item.getId());
        rowResult.setCustomerCode(item.getCustomerCode());
        rowResult.setMessage(message);
        return rowResult;
    }

    private Map<String, Integer> resolveHeaderIndexes(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> indexes = new LinkedHashMap<>();
        for (int cellIndex = headerRow.getFirstCellNum(); cellIndex < headerRow.getLastCellNum(); cellIndex++) {
            if (cellIndex < 0) {
                continue;
            }
            String header = readString(headerRow, cellIndex, formatter);
            if (header != null) {
                indexes.put(header, cellIndex);
            }
        }
        return indexes;
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter) {
        for (int cellIndex = row.getFirstCellNum(); cellIndex < row.getLastCellNum(); cellIndex++) {
            if (cellIndex < 0) {
                continue;
            }
            if (trimToNull(readString(row, cellIndex, formatter)) != null) {
                return false;
            }
        }
        return true;
    }

    private String readString(Row row, Integer cellIndex, DataFormatter formatter) {
        if (row == null || cellIndex == null || cellIndex < 0) {
            return null;
        }
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }
        String value = formatter.formatCellValue(cell);
        return trimToNull(value);
    }

    private Long readLong(Row row, Integer cellIndex, DataFormatter formatter) {
        String value = readString(row, cellIndex, formatter);
        return value == null ? null : Long.valueOf(value);
    }

    private void writeCell(Row row, int cellIndex, Object value) {
        if (value == null) {
            return;
        }
        row.createCell(cellIndex).setCellValue(String.valueOf(value));
    }

    private void writeWorkbookResponse(XSSFWorkbook workbook, HttpServletResponse response, String fileName)
            throws IOException {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        workbook.write(response.getOutputStream());
        response.flushBuffer();
    }

    private String resolveExceptionMessage(Exception ex) {
        if (ex instanceof BusinessException businessException && businessException.getMessage() != null) {
            return businessException.getMessage();
        }
        return ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
    }

    private String rowMessage(CustomerImportItemDTO item, String message) {
        return "row " + item.getRowNo() + ": " + message;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String firstNonBlank(String primary, String fallback) {
        String trimmedPrimary = trimToNull(primary);
        return trimmedPrimary != null ? trimmedPrimary : trimToNull(fallback);
    }

    private <T> T firstNonNull(T primary, T fallback) {
        return primary != null ? primary : fallback;
    }

    private StatusEnum parseStatus(String raw) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        if (Objects.equals(value, "1") || Objects.equals(value, "有効")) {
            return StatusEnum.NOMAL;
        }
        if (Objects.equals(value, "0") || Objects.equals(value, "無効")) {
            return StatusEnum.FOBBIDEN;
        }
        throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "unknown status: " + value);
    }

    private StatusEnum parseStatus(String raw, Integer fallbackCode) {
        StatusEnum parsed = parseStatus(raw);
        return parsed != null ? parsed : fallbackCode == null ? null : StatusEnum.fromValue(fallbackCode);
    }

    private String renderStatus(Integer statusCode) {
        if (statusCode == null) {
            return null;
        }
        StatusEnum statusEnum = StatusEnum.fromValue(statusCode);
        if (statusEnum == null) {
            return String.valueOf(statusCode);
        }
        return statusEnum == StatusEnum.NOMAL ? "有効" : "無効";
    }
}
