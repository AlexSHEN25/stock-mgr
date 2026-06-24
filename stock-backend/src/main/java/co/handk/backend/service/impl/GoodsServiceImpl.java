package co.handk.backend.service.impl;

import co.handk.backend.entity.Goods;
import co.handk.backend.entity.GoodsImage;
import co.handk.backend.entity.GoodsSku;
import co.handk.backend.entity.Brand;
import co.handk.backend.entity.BrandSeriesMakerRelation;
import co.handk.backend.entity.Category;
import co.handk.backend.entity.Maker;
import co.handk.backend.entity.Series;
import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.mapper.BrandSeriesMakerRelationMapper;
import co.handk.backend.mapper.GoodsMapper;
import co.handk.backend.constant.UploadBizType;
import co.handk.backend.service.BrandSeriesMakerRelationService;
import co.handk.backend.service.BrandService;
import co.handk.backend.service.CategoryService;
import co.handk.backend.service.FileStorageService;
import co.handk.backend.service.GoodsImageService;
import co.handk.backend.service.GoodsService;
import co.handk.backend.service.GoodsSkuService;
import co.handk.backend.service.MakerService;
import co.handk.backend.service.SeriesService;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.GoodsImportConstant;
import co.handk.common.constant.NumberConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.GoodsBatchActionEnum;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsDTO;
import co.handk.common.model.dto.goods.GoodsBatchUpsertDTO;
import co.handk.common.model.dto.goods.GoodsBatchUpsertItemDTO;
import co.handk.common.model.dto.query.GoodsQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsDTO;
import co.handk.common.model.vo.GoodsBatchUpsertResultVO;
import co.handk.common.model.vo.GoodsBatchUpsertRowResultVO;
import co.handk.common.model.vo.GoodsListVO;
import co.handk.common.model.vo.GoodsVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
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
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
public class GoodsServiceImpl extends BaseServiceImpl<GoodsMapper, Goods, GoodsVO>
        implements GoodsService {

    private static final ConcurrentMap<String, Object> MASTER_DATA_LOCKS = new ConcurrentHashMap<>();
    private static final String TEMPLATE_SHEET_NAME = GoodsImportConstant.SHEET_TEMPLATE;
    private static final String INSTRUCTION_SHEET_NAME = GoodsImportConstant.SHEET_INSTRUCTION;
    private static final String DICTIONARY_SHEET_NAME = GoodsImportConstant.SHEET_DICTIONARY;
    private static final int TEMPLATE_HEADER_ROW_INDEX = 0;
    private static final int TEMPLATE_NOTE_ROW_INDEX = 1;
    private static final int TEMPLATE_DATA_START_ROW_INDEX = 2;
    private static final int EXCEL_COLUMN_WIDTH = 18 * 256;
    private static final int INSTRUCTION_COLUMN_WIDTH = 90 * 256;
    private static final int DICTIONARY_ID_COLUMN_WIDTH = 20 * 256;
    private static final int DICTIONARY_NAME_COLUMN_WIDTH = 28 * 256;
    private static final int TEMPLATE_VALIDATION_MAX_ROW = 5000;
    private static final long EXPORT_MAX_ROWS = 10_000L;
    private static final String EXPORT_GOODS_FILE_NAME = "goods_export.xlsx";
    private static final String TEMPLATE_VALIDATION_SHEET_NAME = "_goods_validation";
    private static final String IMPORT_RESULT_ACTION_HEADER = "Import Action";
    private static final String IMPORT_RESULT_MESSAGE_HEADER = "Import Message";
    private static final String EXPORT_GOODS_SHEET_NAME = "商品一覧";
    private static final String ERROR_EXPORT_FAILED = "Excel出力に失敗しました";
    private static final String[] GOODS_EXPORT_HEADERS = {
            "ID", "商品名", "英語名", "ブランド", "シリーズ", "カテゴリ",
            "メーカー", "SKUコード", "SKU名", "価格", "通貨", "状態", "説明", "表示順"
    };
    private static final String ROW_PREFIX = "行 ";
    private static final String ERROR_IMPORT_FILE_REQUIRED = "インポートファイルを選択してください";
    private static final String ERROR_TEMPLATE_GENERATE_FAILED = "商品インポートテンプレートの生成に失敗しました";
    private static final String ERROR_CREATE_GOODS_FAILED = "商品の登録に失敗しました";
    private static final String ERROR_UPDATE_GOODS_FAILED = "商品の更新に失敗しました";
    private static final String ERROR_TEMPLATE_SHEET_NOT_FOUND = "テンプレートシートが見つかりません";
    private static final String ERROR_HEADER_ROW_MISSING = "ヘッダー行が見つかりません";
    private static final String ERROR_IMPORT_FILE_READ_FAILED = "インポートファイルの読み取りに失敗しました";
    private static final String ERROR_TEMPLATE_COLUMN_MISSING = "テンプレート列が不足しています: ";
    private static final String ERROR_UNKNOWN = "不明なエラー";
    private static final DateTimeFormatter TEMPLATE_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String HEADER_GOODS_ID = "商品ID";
    private static final String HEADER_SKU_ID = "SKU ID";
    private static final String HEADER_GOODS_NAME = "商品名称";
    private static final String HEADER_ENGLISH_NAME = "英文名称";
    private static final String HEADER_BRAND_ID = "品牌ID";
    private static final String HEADER_BRAND_NAME = "品牌名称";
    private static final String HEADER_SERIES_ID = "系列ID";
    private static final String HEADER_SERIES_NAME = "系列名称";
    private static final String HEADER_CATEGORY_ID = "分类ID";
    private static final String HEADER_CATEGORY_NAME = "分类名称";
    private static final String HEADER_MAKER_ID = "厂家ID";
    private static final String HEADER_MAKER_NAME = "厂家名称";
    private static final String HEADER_DESCRIPTION = "说明";
    private static final String HEADER_IS_HOT = "热门";
    private static final String HEADER_SORT = "排序";
    private static final String HEADER_SKU_CODE = "SKU编码";
    private static final String HEADER_SKU_NAME = "SKU名称";
    private static final String HEADER_PRICE = "价格";
    private static final String HEADER_CURRENCY = "货币";
    private static final String HEADER_COST_PRICE = "成本价";
    private static final String HEADER_UPDATE_PRICE = "调价";
    private static final String HEADER_PRICE_UPDATE_TIME = "调价时间";
    private static final String HEADER_BARCODE = "条码";
    private static final String HEADER_WEIGHT = "重量";
    private static final String HEADER_VOLUME = "体积";
    private static final String HEADER_SKU_STATUS = "SKU状态";
    private static final String HEADER_IMAGE_ID = "图片ID";
    private static final String HEADER_IMAGE_URL = "图片URL";
    private static final String HEADER_IMAGE_SORT = "图片排序";
    private static final String HEADER_GOODS_STATUS = "商品状态";
    private static final String TEMPLATE_HEADER_GOODS_ID = "商品ID";
    private static final String TEMPLATE_HEADER_SKU_ID = "SKU ID";
    private static final String TEMPLATE_HEADER_GOODS_NAME = "商品名";
    private static final String TEMPLATE_HEADER_ENGLISH_NAME = "英語名";
    private static final String TEMPLATE_HEADER_BRAND_NAME = "ブランド名";
    private static final String TEMPLATE_HEADER_SERIES_NAME = "シリーズ名";
    private static final String TEMPLATE_HEADER_CATEGORY_NAME = "分類名";
    private static final String TEMPLATE_HEADER_MAKER_NAME = "メーカー名";
    private static final String TEMPLATE_HEADER_DESCRIPTION = "説明";
    private static final String TEMPLATE_HEADER_IS_HOT = "人気商品";
    private static final String TEMPLATE_HEADER_SORT = "並び順";
    private static final String TEMPLATE_HEADER_SKU_CODE = "SKUコード";
    private static final String TEMPLATE_HEADER_SKU_NAME = "SKU名";
    private static final String TEMPLATE_HEADER_PRICE = "販売価格";
    private static final String TEMPLATE_HEADER_CURRENCY = "通貨";
    private static final String TEMPLATE_HEADER_COST_PRICE = "原価";
    private static final String TEMPLATE_HEADER_UPDATE_PRICE = "更新価格";
    private static final String TEMPLATE_HEADER_PRICE_UPDATE_TIME = "価格更新日時";
    private static final String TEMPLATE_HEADER_BARCODE = "バーコード";
    private static final String TEMPLATE_HEADER_WEIGHT = "重量";
    private static final String TEMPLATE_HEADER_VOLUME = "体積";
    private static final String TEMPLATE_HEADER_SKU_STATUS = "SKU状態";
    private static final String TEMPLATE_HEADER_IMAGE_ID = "画像ID";
    private static final String TEMPLATE_HEADER_IMAGE_URL = "画像URL";
    private static final String TEMPLATE_HEADER_IMAGE_SORT = "画像順";
    private static final String TEMPLATE_HEADER_GOODS_STATUS = "商品状態";
    private static final List<String> TEMPLATE_HEADERS = List.of(
            TEMPLATE_HEADER_GOODS_ID,
            TEMPLATE_HEADER_SKU_ID,
            TEMPLATE_HEADER_GOODS_NAME,
            TEMPLATE_HEADER_ENGLISH_NAME,
            TEMPLATE_HEADER_BRAND_NAME,
            TEMPLATE_HEADER_SERIES_NAME,
            TEMPLATE_HEADER_CATEGORY_NAME,
            TEMPLATE_HEADER_MAKER_NAME,
            TEMPLATE_HEADER_SKU_CODE,
            TEMPLATE_HEADER_SKU_NAME,
            TEMPLATE_HEADER_PRICE,
            TEMPLATE_HEADER_CURRENCY
    );
    private static final List<String> TEMPLATE_REQUIRED_HEADERS = List.of(
            TEMPLATE_HEADER_GOODS_ID,
            TEMPLATE_HEADER_SKU_ID,
            TEMPLATE_HEADER_GOODS_NAME,
            TEMPLATE_HEADER_BRAND_NAME,
            TEMPLATE_HEADER_CATEGORY_NAME,
            TEMPLATE_HEADER_SKU_CODE
    );

    private final GoodsSkuService goodsSkuService;
    private final GoodsImageService goodsImageService;
    private final FileStorageService fileStorageService;
    private final BrandService brandService;
    private final SeriesService seriesService;
    private final CategoryService categoryService;
    private final MakerService makerService;
    private final BrandSeriesMakerRelationService brandSeriesMakerRelationService;
    private final BrandSeriesMakerRelationMapper brandSeriesMakerRelationMapper;
    private final ApplicationContext applicationContext;
    private final PlatformTransactionManager transactionManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveGoods(CreateGoodsDTO dto) {
        validatePriceUpdateFields(dto.getUpdatePrice(), dto.getPriceUpdateTime());
        Goods goods = new Goods();
        BeanUtils.copyProperties(dto, goods);
        if (!StringUtils.hasText(goods.getName())) {
            goods.setName(StringUtils.hasText(dto.getSkuName()) ? dto.getSkuName() : dto.getSkuCode());
        }
        if (!StringUtils.hasText(goods.getEnglishName())) {
            goods.setEnglishName(goods.getName());
        }
        goods.setStatus(dto.getStatus() == null ? StatusEnum.NOMAL.getCode() : dto.getStatus().getCode());
        boolean goodsSaved = this.save(goods);
        if (!goodsSaved) {
            return false;
        }
        syncCascadingRelations(goods.getBrandId(), goods.getSeriesId(), goods.getMakerId());

        GoodsSku sku = new GoodsSku();
        sku.setGoodsId(goods.getId());
        sku.setSkuCode(dto.getSkuCode());
        sku.setSkuName(StringUtils.hasText(dto.getSkuName()) ? dto.getSkuName() : goods.getName());
        sku.setPrice(dto.getPrice());
        sku.setCurrency(StringUtils.hasText(dto.getCurrency()) ? dto.getCurrency() : CommonConstant.DEFAULT_CURRENCY_JPY);
        sku.setCostPrice(dto.getCostPrice());
        sku.setUpdatePrice(dto.getUpdatePrice());
        sku.setPriceUpdateTime(dto.getPriceUpdateTime());
        sku.setBarcode(StringUtils.hasText(dto.getBarcode()) ? dto.getBarcode() : generateBarcode());
        sku.setWeight(dto.getWeight());
        sku.setVolume(dto.getVolume());
        sku.setStatus(dto.getSkuStatus() == null ? StatusEnum.NOMAL.getCode() : dto.getSkuStatus().getCode());
        boolean skuSaved = goodsSkuService.save(sku);
        if (!skuSaved) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "SKUの保存に失敗しました");
        }

        if (StringUtils.hasText(dto.getImageUrl())) {
            String normalizedImageUrl = fileStorageService.normalize(UploadBizType.GOODS, dto.getImageUrl());
            GoodsImage image = new GoodsImage();
            image.setGoodsId(goods.getId());
            image.setSkuId(sku.getId());
            image.setSkuCode(sku.getSkuCode());
            image.setImageUrl(normalizedImageUrl);
            image.setSort(dto.getImageSort() == null ? NumberConstant.ZERO : dto.getImageSort());
            boolean imageSaved = goodsImageService.save(image);
            if (!imageSaved) {
                throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "商品画像の保存に失敗しました");
            }
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public GoodsVO getGoodsById(Long id) {
        Goods goods = this.getByIdNotDeleted(id);
        if (goods == null) {
            return null;
        }
        GoodsVO vo = toVO(goods);
        GoodsSku sku = goodsSkuService.getOne(new QueryWrapper<GoodsSku>()
                .eq("goods_id", id)
                .orderByDesc("update_time")
                .last("LIMIT 1"));
        if (sku != null) {
            vo.setSkuCode(sku.getSkuCode());
            vo.setSkuName(sku.getSkuName());
            vo.setPrice(sku.getPrice());
            vo.setCurrency(sku.getCurrency());
            vo.setCostPrice(sku.getCostPrice());
            vo.setUpdatePrice(sku.getUpdatePrice());
            vo.setPriceUpdateTime(sku.getPriceUpdateTime());
            vo.setBarcode(sku.getBarcode());
            vo.setWeight(sku.getWeight());
            vo.setVolume(sku.getVolume());
        }
        GoodsImage image = goodsImageService.getOne(new QueryWrapper<GoodsImage>()
                .eq("goods_id", id)
                .orderByAsc("sort", "id")
                .last("LIMIT 1"));
        if (image != null) {
            vo.setImageId(image.getId());
            vo.setImageUrl(fileStorageService.toApiPath(UploadBizType.GOODS, image.getImageUrl()));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateGoods(UpdateGoodsDTO dto) {
        validatePriceUpdateFields(dto.getUpdatePrice(), dto.getPriceUpdateTime());
        Goods existed = this.getByIdNotDeleted(dto.getId());
        if (existed == null) {
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "商品不存在");
        }
        Goods goods = new Goods();
        BeanUtils.copyProperties(dto, goods);
        goods.setStatus(dto.getStatus() == null ? null : dto.getStatus().getCode());
        boolean goodsUpdated = super.updateByDto(goods);
        if (!goodsUpdated) {
            return false;
        }
        syncCascadingRelations(goods.getBrandId(), goods.getSeriesId(), goods.getMakerId());
        cleanupCascadingRelations(existed.getBrandId(), existed.getSeriesId(), existed.getMakerId());

        String skuNameToSave = StringUtils.hasText(dto.getSkuName()) ? dto.getSkuName() : dto.getName();
        Integer skuStatusCode = dto.getSkuStatus() == null ? null : dto.getSkuStatus().getCode();
        UpdateWrapper<GoodsSku> skuWrapper = new UpdateWrapper<GoodsSku>()
                .eq("goods_id", dto.getId())
                .set(StringUtils.hasText(dto.getSkuCode()), "sku_code", dto.getSkuCode())
                .set(StringUtils.hasText(skuNameToSave), "sku_name", skuNameToSave)
                .set(dto.getPrice() != null, "price", dto.getPrice())
                .set(StringUtils.hasText(dto.getCurrency()), "currency", dto.getCurrency())
                .set(dto.getCostPrice() != null, "cost_price", dto.getCostPrice())
                .set(dto.getUpdatePrice() != null, "update_price", dto.getUpdatePrice())
                .set(dto.getPriceUpdateTime() != null, "price_update_time", dto.getPriceUpdateTime())
                .set(StringUtils.hasText(dto.getBarcode()), "barcode", dto.getBarcode())
                .set(dto.getWeight() != null, "weight", dto.getWeight())
                .set(dto.getVolume() != null, "volume", dto.getVolume())
                .set(skuStatusCode != null, "status", skuStatusCode);
        if (dto.getSkuId() != null) {
            skuWrapper.eq("id", dto.getSkuId());
        }
        boolean skuUpdated = goodsSkuService.update(null, skuWrapper);
        if (!skuUpdated) {
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "SKUの更新に失敗しました");
        }

        if (StringUtils.hasText(dto.getImageUrl()) || dto.getImageSort() != null || dto.getImageId() != null) {
            GoodsImage existedImage = resolveTargetGoodsImage(dto);
            String oldImageUrl = existedImage == null ? null : existedImage.getImageUrl();
            String normalizedImageUrl = fileStorageService.normalize(UploadBizType.GOODS, dto.getImageUrl());
            UpdateWrapper<GoodsImage> imageWrapper = new UpdateWrapper<GoodsImage>()
                    .eq("goods_id", dto.getId())
                    .set(StringUtils.hasText(normalizedImageUrl), "image_url", normalizedImageUrl)
                    .set(dto.getImageSort() != null, "sort", dto.getImageSort())
                    .set(StringUtils.hasText(dto.getSkuCode()), "sku_code", dto.getSkuCode());
            if (dto.getImageId() != null) {
                imageWrapper.eq("id", dto.getImageId());
            }
            try {
            boolean imageUpdated = goodsImageService.update(null, imageWrapper);
            if (!imageUpdated) {
                throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "商品画像の更新に失敗しました");
            }
            } catch (RuntimeException ex) {
                if (StringUtils.hasText(normalizedImageUrl) && !Objects.equals(normalizedImageUrl, oldImageUrl)) {
                    fileStorageService.delete(UploadBizType.GOODS, normalizedImageUrl);
                }
                throw ex;
            }
            if (StringUtils.hasText(normalizedImageUrl) && !Objects.equals(normalizedImageUrl, oldImageUrl)) {
                fileStorageService.delete(UploadBizType.GOODS, oldImageUrl);
            }
        }
        return true;
    }

    private GoodsImage resolveTargetGoodsImage(UpdateGoodsDTO dto) {
        if (dto == null) {
            return null;
        }
        if (dto.getImageId() != null) {
            return goodsImageService.getByIdNotDeleted(dto.getImageId());
        }
        return goodsImageService.getOne(new QueryWrapper<GoodsImage>()
                .eq("goods_id", dto.getId())
                .orderByAsc("sort", "id")
                .last("LIMIT 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteGoodsById(Long id) {
        Goods existed = this.getByIdNotDeleted(id);
        if (existed == null) {
            return 0;
        }
        int goodsRows = super.deleteByIdLogic(id);
        goodsSkuService.update(null, new UpdateWrapper<GoodsSku>()
                .eq("goods_id", id)
                .set("deleted", DeleteEnum.DELETED.getCode()));
        goodsImageService.update(null, new UpdateWrapper<GoodsImage>()
                .eq("goods_id", id)
                .set("deleted", DeleteEnum.DELETED.getCode()));
        cleanupCascadingRelations(existed.getBrandId(), existed.getSeriesId(), existed.getMakerId());
        return goodsRows;
    }

    @Override
    public GoodsBatchUpsertResultVO batchUpsertGoods(GoodsBatchUpsertDTO dto) {
        return batchUpsertItems(dto == null ? List.of() : dto.getItems());
    }

    @Override
    public GoodsBatchUpsertResultVO importGoods(MultipartFile file) {
        return importGoods(file, null);
    }

    @Override
    public GoodsBatchUpsertResultVO importGoods(MultipartFile file, GoodsQueryDTO query) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_IMPORT_FILE_REQUIRED);
        }
        try {
            byte[] fileBytes = file.getBytes();
            GoodsBatchUpsertResultVO result = batchUpsertItems(parseGoodsImportFile(fileBytes), query);
            if (result.getFailureCount() != null && result.getFailureCount() > 0) {
                attachImportErrorReport(file.getOriginalFilename(), fileBytes, result);
            }
            return result;
        } catch (IOException ex) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_IMPORT_FILE_READ_FAILED, ex);
        }
    }

    @Override
    public void downloadBatchTemplate(HttpServletResponse response) {
        downloadBatchTemplate(null, response);
    }

    @Override
    public void downloadBatchTemplate(GoodsQueryDTO query, HttpServletResponse response) {
        try (XSSFWorkbook workbook = buildBatchTemplateWorkbook(query)) {
            String fileName = GoodsImportConstant.TEMPLATE_FILE_NAME;
            response.setContentType(GoodsImportConstant.EXCEL_CONTENT_TYPE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            workbook.write(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ex) {
            throw new BusinessException(
                    MessageKeyConstant.ERROR_RUNTIME,
                    ERROR_TEMPLATE_GENERATE_FAILED,
                    ex
            );
        }
    }

    private void syncCascadingRelations(Long brandId, Long seriesId, Long makerId) {
        // The canonical hierarchy now lives on series.brand_id and maker.series_id.
    }

    private void cleanupCascadingRelations(Long brandId, Long seriesId, Long makerId) {
        // Legacy relation cleanup is intentionally disabled after the hierarchy switch.
    }

    private boolean existsGoodsWithRelation(Long brandId, Long seriesId, Long makerId) {
        return this.count(new QueryWrapper<Goods>()
                .eq("brand_id", brandId)
                .eq("series_id", seriesId)
                .eq("maker_id", makerId)) > 0;
    }

    private GoodsBatchUpsertResultVO batchUpsertItems(List<GoodsBatchUpsertItemDTO> items) {
        return batchUpsertItems(items, null);
    }

    private GoodsBatchUpsertResultVO batchUpsertItems(List<GoodsBatchUpsertItemDTO> items, GoodsQueryDTO scopeQuery) {
        List<GoodsBatchUpsertItemDTO> safeItems = items == null ? List.of() : items.stream()
                .filter(Objects::nonNull)
                .toList();
        GoodsBatchUpsertResultVO result = new GoodsBatchUpsertResultVO();
        result.setTotalCount(safeItems.size());
        result.setSuccessCount(0);
        result.setCreatedCount(0);
        result.setUpdatedCount(0);
        result.setFailureCount(0);
        if (safeItems.isEmpty()) {
            return result;
        }

        List<GoodsBatchUpsertItemDTO> validatedItems = new ArrayList<>();
        Map<String, Integer> skuCodeRows = new HashMap<>();
        Map<Long, Integer> goodsIdRows = new HashMap<>();
        Map<Long, Integer> skuIdRows = new HashMap<>();
        for (int index = 0; index < safeItems.size(); index++) {
            GoodsBatchUpsertItemDTO item = safeItems.get(index);
            int rowNo = item.getRowNo() == null ? index + 1 : item.getRowNo();
            item.setRowNo(rowNo);
            try {
                validateBatchIdentityUniqueness(item, skuCodeRows, goodsIdRows, skuIdRows);
                validateItemWithinScope(item, scopeQuery);
                validatedItems.add(item);
            } catch (Exception ex) {
                result.getRows().add(buildFailedRowResult(item, resolveExceptionMessage(ex)));
                result.setFailureCount(result.getFailureCount() + 1);
            }
        }
        if (result.getFailureCount() > 0) {
            return result;
        }

        try {
            executeBatchItemsInSingleTransaction(validatedItems, result);
        } catch (Exception ex) {
            String message = "batch rolled back: " + resolveExceptionMessage(ex);
            result.setRows(new ArrayList<>());
            result.setSuccessCount(0);
            result.setCreatedCount(0);
            result.setUpdatedCount(0);
            result.setFailureCount(validatedItems.size());
            for (GoodsBatchUpsertItemDTO item : validatedItems) {
                result.getRows().add(buildFailedRowResult(item, message));
            }
        }
        return result;
    }

    private GoodsService goodsServiceProxy() {
        return applicationContext.getBean(GoodsService.class);
    }

    private void executeBatchItemsInSingleTransaction(List<GoodsBatchUpsertItemDTO> items, GoodsBatchUpsertResultVO result) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        template.executeWithoutResult(status -> {
            for (GoodsBatchUpsertItemDTO item : items) {
                PreparedBatchItem preparedItem = prepareBatchItem(item);
                GoodsBatchUpsertRowResultVO committed = persistPreparedBatchItem(preparedItem);
                result.getRows().add(committed);
                result.setSuccessCount(result.getSuccessCount() + 1);
                if (GoodsBatchActionEnum.CREATED.getCode().equals(committed.getAction())) {
                    result.setCreatedCount(result.getCreatedCount() + 1);
                } else {
                    result.setUpdatedCount(result.getUpdatedCount() + 1);
                }
            }
        });
    }

    private PreparedBatchItem prepareBatchItem(GoodsBatchUpsertItemDTO item) {
        ExistingGoodsTarget target = resolveExistingTarget(item);
        if (target == null) {
            return new PreparedBatchItem(
                    item,
                    GoodsBatchActionEnum.CREATED.getCode(),
                    buildCreateGoodsDto(item),
                    null
            );
        }
        return new PreparedBatchItem(
                item,
                GoodsBatchActionEnum.UPDATED.getCode(),
                null,
                buildUpdateGoodsDto(item, target)
        );
    }

    private GoodsBatchUpsertRowResultVO persistPreparedBatchItem(PreparedBatchItem preparedItem) {
        GoodsBatchUpsertItemDTO item = preparedItem.item();
        GoodsBatchUpsertRowResultVO rowResult = new GoodsBatchUpsertRowResultVO();
        rowResult.setRowNo(item.getRowNo());
        rowResult.setSkuCode(trimToNull(item.getSkuCode()));
        if (GoodsBatchActionEnum.CREATED.getCode().equals(preparedItem.action())) {
            if (!goodsServiceProxy().saveGoods(preparedItem.createDto())) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_CREATE_GOODS_FAILED);
            }
            ExistingGoodsTarget saved = resolveExistingTarget(item);
            rowResult.setSuccess(true);
            rowResult.setAction(GoodsBatchActionEnum.CREATED.getCode());
            rowResult.setGoodsId(saved == null || saved.goods() == null ? null : saved.goods().getId());
            rowResult.setSkuId(saved == null || saved.sku() == null ? null : saved.sku().getId());
            rowResult.setMessage(GoodsBatchActionEnum.CREATED.getMessage());
            return rowResult;
        }

        if (!goodsServiceProxy().updateGoods(preparedItem.updateDto())) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_UPDATE_GOODS_FAILED);
        }
        ExistingGoodsTarget updated = resolveExistingTarget(item);
        rowResult.setSuccess(true);
        rowResult.setAction(GoodsBatchActionEnum.UPDATED.getCode());
        rowResult.setGoodsId(updated == null || updated.goods() == null ? null : updated.goods().getId());
        rowResult.setSkuId(updated == null || updated.sku() == null ? null : updated.sku().getId());
        rowResult.setMessage(GoodsBatchActionEnum.UPDATED.getMessage());
        return rowResult;
    }

    private GoodsBatchUpsertRowResultVO buildFailedRowResult(GoodsBatchUpsertItemDTO item, String message) {
        GoodsBatchUpsertRowResultVO rowResult = new GoodsBatchUpsertRowResultVO();
        rowResult.setRowNo(item.getRowNo());
        rowResult.setSkuCode(trimToNull(item.getSkuCode()));
        rowResult.setSuccess(false);
        rowResult.setAction(GoodsBatchActionEnum.FAILED.getCode());
        rowResult.setMessage(message);
        return rowResult;
    }

    private void validateBatchIdentityUniqueness(GoodsBatchUpsertItemDTO item,
                                                 Map<String, Integer> skuCodeRows,
                                                 Map<Long, Integer> goodsIdRows,
                                                 Map<Long, Integer> skuIdRows) {
        String skuCode = trimToNull(item.getSkuCode());
        if (skuCode != null) {
            Integer existingRow = skuCodeRows.putIfAbsent(skuCode, item.getRowNo());
            if (existingRow != null) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                        rowMessage(item, "duplicate skuCode in batch, first row: " + existingRow));
            }
        }
        if (item.getGoodsId() != null) {
            Integer existingRow = goodsIdRows.putIfAbsent(item.getGoodsId(), item.getRowNo());
            if (existingRow != null) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                        rowMessage(item, "duplicate goodsId in batch, first row: " + existingRow));
            }
        }
        if (item.getSkuId() != null) {
            Integer existingRow = skuIdRows.putIfAbsent(item.getSkuId(), item.getRowNo());
            if (existingRow != null) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                        rowMessage(item, "duplicate skuId in batch, first row: " + existingRow));
            }
        }
    }

    private CreateGoodsDTO buildCreateGoodsDto(GoodsBatchUpsertItemDTO item) {
        CreateGoodsDTO dto = new CreateGoodsDTO();
        Long brandId = resolveOrCreateBrandId(item);
        Long categoryId = resolveOrCreateCategoryId(item);
        Long seriesId = resolveOrCreateSeriesId(item, brandId);
        Long makerId = resolveOrCreateMakerId(item, seriesId);
        String skuCode = trimToNull(item.getSkuCode());
        String name = firstNonBlank(item.getName(), item.getSkuName(), skuCode);
        if (!StringUtils.hasText(name)) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, "商品名称またはSKU编码を入力してください"));
        }
        if (brandId == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, "ブランドを入力してください"));
        }
        if (categoryId == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, "カテゴリを入力してください"));
        }
        if (!StringUtils.hasText(skuCode)) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, "SKU编码を入力してください"));
        }
        dto.setName(name);
        dto.setEnglishName(firstNonBlank(item.getEnglishName(), name));
        dto.setBrandId(brandId);
        dto.setSeriesId(seriesId);
        dto.setCategoryId(categoryId);
        dto.setMakerId(makerId);
        dto.setDescription(trimToNull(item.getDescription()));
        dto.setIsHot(parseFlag(item.getIsHot()));
        dto.setSort(item.getSort());
        dto.setSkuCode(skuCode);
        dto.setSkuName(firstNonBlank(item.getSkuName(), name));
        dto.setPrice(item.getPrice());
        dto.setCurrency(firstNonBlank(item.getCurrency(), CommonConstant.DEFAULT_CURRENCY_JPY));
        dto.setCostPrice(item.getCostPrice());
        dto.setUpdatePrice(item.getUpdatePrice());
        dto.setPriceUpdateTime(normalizePriceUpdateTime(item.getUpdatePrice(), item.getPriceUpdateTime()));
        dto.setBarcode(trimToNull(item.getBarcode()));
        dto.setWeight(item.getWeight());
        dto.setVolume(item.getVolume());
        dto.setSkuStatus(parseStatus(item.getSkuStatus()));
        dto.setImageUrl(trimToNull(item.getImageUrl()));
        dto.setImageSort(item.getImageSort());
        dto.setStatus(parseStatus(item.getStatus()));
        return dto;
    }

    private UpdateGoodsDTO buildUpdateGoodsDto(GoodsBatchUpsertItemDTO item, ExistingGoodsTarget target) {
        Goods goods = target.goods();
        GoodsSku sku = target.sku();
        GoodsImage image = target.image();
        UpdateGoodsDTO dto = new UpdateGoodsDTO();
        dto.setId(goods.getId());
        dto.setName(firstNonBlank(item.getName(), goods.getName(), sku == null ? null : sku.getSkuName(),
                item.getSkuCode(), sku == null ? null : sku.getSkuCode()));
        dto.setEnglishName(firstNonBlank(item.getEnglishName(), goods.getEnglishName(), dto.getName()));
        Long brandId = firstNonNull(resolveOrCreateBrandId(item), goods.getBrandId());
        Long categoryId = firstNonNull(resolveOrCreateCategoryId(item), goods.getCategoryId());
        Long seriesId = firstNonNull(resolveOrCreateSeriesId(item, brandId), goods.getSeriesId());
        dto.setBrandId(brandId);
        dto.setSeriesId(seriesId);
        dto.setCategoryId(categoryId);
        dto.setMakerId(firstNonNull(resolveOrCreateMakerId(item, seriesId), goods.getMakerId()));
        dto.setDescription(firstNonNull(trimToNull(item.getDescription()), goods.getDescription()));
        dto.setIsHot(firstNonNull(parseFlag(item.getIsHot(), false), goods.getIsHot()));
        dto.setSort(firstNonNull(item.getSort(), goods.getSort()));
        dto.setSkuId(item.getSkuId() != null ? item.getSkuId() : sku == null ? null : sku.getId());
        dto.setSkuCode(firstNonBlank(item.getSkuCode(), sku == null ? null : sku.getSkuCode()));
        dto.setSkuName(firstNonBlank(item.getSkuName(), sku == null ? null : sku.getSkuName(), dto.getName()));
        dto.setPrice(firstNonNull(item.getPrice(), sku == null ? null : sku.getPrice()));
        dto.setCurrency(firstNonBlank(item.getCurrency(),
                sku == null ? null : sku.getCurrency(), CommonConstant.DEFAULT_CURRENCY_JPY));
        dto.setCostPrice(firstNonNull(item.getCostPrice(), sku == null ? null : sku.getCostPrice()));
        dto.setUpdatePrice(firstNonNull(item.getUpdatePrice(), sku == null ? null : sku.getUpdatePrice()));
        dto.setPriceUpdateTime(normalizePriceUpdateTime(dto.getUpdatePrice(),
                firstNonNull(item.getPriceUpdateTime(), sku == null ? null : sku.getPriceUpdateTime())));
        dto.setBarcode(firstNonBlank(item.getBarcode(), sku == null ? null : sku.getBarcode()));
        dto.setWeight(firstNonNull(item.getWeight(), sku == null ? null : sku.getWeight()));
        dto.setVolume(firstNonNull(item.getVolume(), sku == null ? null : sku.getVolume()));
        dto.setSkuStatus(firstNonNull(parseStatus(item.getSkuStatus(), false),
                sku == null ? null : StatusEnum.fromValue(sku.getStatus())));
        dto.setImageId(item.getImageId() != null ? item.getImageId() : image == null ? null : image.getId());
        dto.setImageUrl(firstNonBlank(item.getImageUrl(),
                image == null ? null : fileStorageService.toApiPath(UploadBizType.GOODS, image.getImageUrl())));
        dto.setImageSort(firstNonNull(item.getImageSort(), image == null ? null : image.getSort()));
        dto.setStatus(firstNonNull(parseStatus(item.getStatus(), false), StatusEnum.fromValue(goods.getStatus())));
        return dto;
    }

    private ExistingGoodsTarget resolveExistingTarget(GoodsBatchUpsertItemDTO item) {
        Goods goods = null;
        GoodsSku sku = null;
        if (item.getGoodsId() != null) {
            goods = this.getByIdNotDeleted(item.getGoodsId());
            if (goods == null) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                        rowMessage(item, "商品IDに該当する商品が見つかりません"));
            }
        }
        if (item.getSkuId() != null) {
            sku = goodsSkuService.getByIdNotDeleted(item.getSkuId());
            if (sku == null) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                        rowMessage(item, "SKU IDに該当するSKUが見つかりません"));
            }
            Goods skuGoods = this.getByIdNotDeleted(sku.getGoodsId());
            if (goods != null && !goods.getId().equals(skuGoods == null ? null : skuGoods.getId())) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                        rowMessage(item, "商品IDとSKU IDが一致しません"));
            }
            goods = skuGoods;
        }
        String skuCode = trimToNull(item.getSkuCode());
        if (goods == null && sku == null && StringUtils.hasText(skuCode)) {
            sku = goodsSkuService.getOne(new QueryWrapper<GoodsSku>()
                    .eq("sku_code", skuCode)
                    .last("LIMIT 1"));
            if (sku != null) {
                goods = this.getByIdNotDeleted(sku.getGoodsId());
            }
        }
        if (goods == null) {
            return null;
        }
        if (sku == null) {
            sku = goodsSkuService.getOne(new QueryWrapper<GoodsSku>()
                    .eq("goods_id", goods.getId())
                    .orderByDesc("update_time", "id")
                    .last("LIMIT 1"));
        }
        GoodsImage image = goodsImageService.getOne(new QueryWrapper<GoodsImage>()
                .eq("goods_id", goods.getId())
                .orderByAsc("sort", "id")
                .last("LIMIT 1"));
        return new ExistingGoodsTarget(goods, sku, image);
    }

    private List<GoodsBatchUpsertItemDTO> parseGoodsImportFile(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            return parseGoodsImportWorkbook(workbook);
        } catch (IOException ex) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_IMPORT_FILE_READ_FAILED, ex);
        }
    }

    private List<GoodsBatchUpsertItemDTO> parseGoodsImportFile(byte[] fileBytes) {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(fileBytes))) {
            return parseGoodsImportWorkbook(workbook);
        } catch (IOException ex) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_IMPORT_FILE_READ_FAILED, ex);
        }
    }

    private List<GoodsBatchUpsertItemDTO> parseGoodsImportWorkbook(Workbook workbook) {
        Sheet sheet = workbook.getSheet(TEMPLATE_SHEET_NAME);
        if (sheet == null) {
            sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
        }
        if (sheet == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_TEMPLATE_SHEET_NOT_FOUND);
        }
        DataFormatter formatter = new DataFormatter();
        Row headerRow = sheet.getRow(TEMPLATE_HEADER_ROW_INDEX);
        if (headerRow == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_HEADER_ROW_MISSING);
        }
        Map<String, Integer> headerIndexes = resolveHeaderIndexes(headerRow, formatter);
        List<GoodsBatchUpsertItemDTO> items = new ArrayList<>();
        for (int rowIndex = TEMPLATE_DATA_START_ROW_INDEX; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null || isTemplateDataRowEmpty(row, formatter, headerIndexes)) {
                continue;
            }
            GoodsBatchUpsertItemDTO item = new GoodsBatchUpsertItemDTO();
            item.setRowNo(rowIndex + 1);
            item.setGoodsId(readLong(row, headerIndexes.get(TEMPLATE_HEADER_GOODS_ID), formatter));
            item.setSkuId(readLong(row, headerIndexes.get(TEMPLATE_HEADER_SKU_ID), formatter));
            item.setName(readString(row, headerIndexes.get(TEMPLATE_HEADER_GOODS_NAME), formatter));
            item.setEnglishName(readString(row, headerIndexes.get(TEMPLATE_HEADER_ENGLISH_NAME), formatter));
            item.setBrandName(readString(row, headerIndexes.get(TEMPLATE_HEADER_BRAND_NAME), formatter));
            item.setSeriesName(readString(row, headerIndexes.get(TEMPLATE_HEADER_SERIES_NAME), formatter));
            item.setCategoryName(readString(row, headerIndexes.get(TEMPLATE_HEADER_CATEGORY_NAME), formatter));
            item.setMakerName(readString(row, headerIndexes.get(TEMPLATE_HEADER_MAKER_NAME), formatter));
            item.setDescription(readString(row, headerIndexes.get(TEMPLATE_HEADER_DESCRIPTION), formatter));
            item.setIsHot(readString(row, headerIndexes.get(TEMPLATE_HEADER_IS_HOT), formatter));
            item.setSort(readInteger(row, headerIndexes.get(TEMPLATE_HEADER_SORT), formatter));
            item.setSkuCode(readString(row, headerIndexes.get(TEMPLATE_HEADER_SKU_CODE), formatter));
            item.setSkuName(readString(row, headerIndexes.get(TEMPLATE_HEADER_SKU_NAME), formatter));
            item.setPrice(readDecimal(row, headerIndexes.get(TEMPLATE_HEADER_PRICE), formatter));
            item.setCurrency(readString(row, headerIndexes.get(TEMPLATE_HEADER_CURRENCY), formatter));
            item.setCostPrice(readDecimal(row, headerIndexes.get(TEMPLATE_HEADER_COST_PRICE), formatter));
            item.setUpdatePrice(readDecimal(row, headerIndexes.get(TEMPLATE_HEADER_UPDATE_PRICE), formatter));
            item.setPriceUpdateTime(readDateTime(row, headerIndexes.get(TEMPLATE_HEADER_PRICE_UPDATE_TIME), formatter));
            item.setBarcode(readString(row, headerIndexes.get(TEMPLATE_HEADER_BARCODE), formatter));
            item.setWeight(readDecimal(row, headerIndexes.get(TEMPLATE_HEADER_WEIGHT), formatter));
            item.setVolume(readDecimal(row, headerIndexes.get(TEMPLATE_HEADER_VOLUME), formatter));
            item.setSkuStatus(readString(row, headerIndexes.get(TEMPLATE_HEADER_SKU_STATUS), formatter));
            item.setImageId(readLong(row, headerIndexes.get(TEMPLATE_HEADER_IMAGE_ID), formatter));
            item.setImageUrl(readString(row, headerIndexes.get(TEMPLATE_HEADER_IMAGE_URL), formatter));
            item.setImageSort(readInteger(row, headerIndexes.get(TEMPLATE_HEADER_IMAGE_SORT), formatter));
            item.setStatus(readString(row, headerIndexes.get(TEMPLATE_HEADER_GOODS_STATUS), formatter));
            items.add(item);
        }
        return items;
    }

    private XSSFWorkbook buildBatchTemplateWorkbook() {
        return buildBatchTemplateWorkbook(null);
    }

    private XSSFWorkbook buildBatchTemplateWorkbook(GoodsQueryDTO scopeQuery) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        TemplateScope scope = loadTemplateScope(scopeQuery);
        buildTemplateSheet(workbook);
        buildInstructionSheet(workbook);
        buildDictionarySheet(workbook, scope);
        buildValidationSheet(workbook, scope);
        applyTemplateValidations(workbook);
        return workbook;
    }

    private void buildTemplateSheet(XSSFWorkbook workbook) {
        Sheet sheet = workbook.createSheet(TEMPLATE_SHEET_NAME);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(TEMPLATE_HEADER_ROW_INDEX);
        for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(TEMPLATE_HEADERS.get(i));
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, EXCEL_COLUMN_WIDTH);
        }

        Row noteRow = sheet.createRow(TEMPLATE_NOTE_ROW_INDEX);
        String[] notes = new String[] {
                "更新时可填，新增留空",
                "更新时可填，新增留空",
                "新增必填；更新建议填写",
                "可空",
                "ID/名称二选一",
                "ID/名称二选一",
                "可空；优先ID",
                "可空；优先ID",
                "ID/名称二选一",
                "ID/名称二选一",
                "可空；优先ID",
                "可空；优先ID",
                "可空",
                "1/0/是/否",
                "可空",
                "新增必填；更新可用于匹配",
                "可空",
                "可空",
                "默认JPY",
                "可空",
                "可空",
                "留空则自动用当前时间",
                "可空",
                "可空",
                "可空",
                "1/0/有効/無効",
                "更新可填",
                "可空",
                "可空",
                "1/0/有効/無効"
        };
        for (int i = 0; i < notes.length; i++) {
            noteRow.createCell(i).setCellValue(notes[i]);
        }
        String[] japaneseNotes = new String[TEMPLATE_HEADERS.size()];
        java.util.Arrays.fill(japaneseNotes, "任意");
        if (japaneseNotes.length > 0) japaneseNotes[0] = "更新時のみ";
        if (japaneseNotes.length > 1) japaneseNotes[1] = "更新時のみ";
        if (japaneseNotes.length > 4) japaneseNotes[4] = "候補から選択 / 未登録なら導入時に作成";
        if (japaneseNotes.length > 5) japaneseNotes[5] = "ブランドに応じて候補表示";
        if (japaneseNotes.length > 6) japaneseNotes[6] = "候補から選択 / 未登録なら導入時に作成";
        if (japaneseNotes.length > 7) japaneseNotes[7] = "シリーズに応じて候補表示";
        if (japaneseNotes.length > 11) japaneseNotes[11] = "必須";
        if (japaneseNotes.length > 14) japaneseNotes[14] = "既定値 JPY";
        if (japaneseNotes.length > 21) japaneseNotes[21] = "1/0 または normal/disabled";
        if (japaneseNotes.length > 22) japaneseNotes[22] = "更新時のみ";
        if (japaneseNotes.length > 25) japaneseNotes[25] = "1/0 または normal/disabled";
        for (int i = 0; i < japaneseNotes.length; i++) {
            noteRow.getCell(i).setCellValue(japaneseNotes[i]);
        }
        for (int i = TEMPLATE_HEADERS.size(); i < noteRow.getLastCellNum(); i++) {
            Cell extraCell = noteRow.getCell(i);
            if (extraCell != null) {
                noteRow.removeCell(extraCell);
            }
        }
        sheet.createFreezePane(0, TEMPLATE_DATA_START_ROW_INDEX);
    }

    private void buildInstructionSheet(XSSFWorkbook workbook) {
        Sheet sheet = workbook.createSheet(INSTRUCTION_SHEET_NAME);
        List<String> instructions = List.of(
                "1. 一行代表一个商品主SKU，系统会按 商品ID / SKU ID / SKU编码 依次匹配现有数据。",
                "2. 匹配到现有商品则更新，匹配不到则新增。",
                "3. 新增时至少需要：商品名称、品牌、分类、SKU编码。",
                "4. 品牌/系列/分类/厂家支持填 ID 或 名称；同时填写时优先 ID。",
                "5. 调价填写后，调价时间留空会自动使用当前时间。",
                "6. 热门支持：1/0/是/否/true/false。",
                "7. 商品状态、SKU状态支持：1/0/有効/無効/normal/disabled。",
                "8. 图片URL 可选；现有商品没有图片时，更新也会自动补建图片记录。"
        );
        for (int i = 0; i < instructions.size(); i++) {
            sheet.createRow(i).createCell(0).setCellValue(instructions.get(i));
        }
        List<String> japaneseInstructions = List.of(
                "1. 商品ID と SKU ID を入力すると既存データ更新、未入力なら新規作成です。",
                "2. ブランド名 / シリーズ名 / 分類名 / メーカー名 は名称で判定します。",
                "3. 主データが未登録の場合は、導入トランザクション内で自動作成します。",
                "4. シリーズはブランド配下、メーカーはシリーズ配下として自動的に関連付けます。",
                "5. SKUコードは新規作成時に必須です。",
                "6. 人気商品は 1/0 または true/false を入力できます。",
                "7. 商品状態・SKU状態は 1/0 または normal/disabled を入力できます。",
                "8. 価格更新日時は yyyy-MM-dd HH:mm:ss 形式です。"
        );
        for (int i = 0; i < japaneseInstructions.size(); i++) {
            sheet.getRow(i).getCell(0).setCellValue(japaneseInstructions.get(i));
        }
        sheet.setColumnWidth(0, INSTRUCTION_COLUMN_WIDTH);
    }

    private void buildDictionarySheet(XSSFWorkbook workbook, TemplateScope scope) {
        Sheet sheet = workbook.createSheet(DICTIONARY_SHEET_NAME);
        int rowIndex = 0;
        if (scope != null) {
            rowIndex = writeDictionarySection(sheet, rowIndex, "brand", scope.brands().stream()
                    .map(item -> List.of(String.valueOf(item.getId()), item.getName(), String.valueOf(item.getStatus())))
                    .toList());
            rowIndex = writeDictionarySection(sheet, rowIndex + 1, "series", scope.series().stream()
                    .map(item -> List.of(
                            String.valueOf(item.getId()),
                            item.getName(),
                            item.getBrandId() == null ? "" : String.valueOf(item.getBrandId())))
                    .toList());
            rowIndex = writeDictionarySection(sheet, rowIndex + 1, "category", scope.categories().stream()
                    .map(item -> List.of(String.valueOf(item.getId()), item.getName(), String.valueOf(item.getStatus())))
                    .toList());
            rowIndex = writeDictionarySection(sheet, rowIndex + 1, "maker", scope.makers().stream()
                    .map(item -> List.of(String.valueOf(item.getId()), item.getName(), String.valueOf(item.getStatus())))
                    .toList());
            writeDictionarySection(sheet, rowIndex + 1, "status", List.of(
                    List.of("1", "normal", "normal"),
                    List.of("0", "disabled", "disabled")
            ));
            sheet.setColumnWidth(0, DICTIONARY_ID_COLUMN_WIDTH);
            sheet.setColumnWidth(1, DICTIONARY_NAME_COLUMN_WIDTH);
            sheet.setColumnWidth(2, DICTIONARY_ID_COLUMN_WIDTH);
            return;
        }
        rowIndex = writeDictionarySection(sheet, rowIndex, "品牌", brandService.list(new QueryWrapper<Brand>()
                .orderByAsc("id")).stream().map(item -> List.of(
                String.valueOf(item.getId()), item.getName(), String.valueOf(item.getStatus()))).toList());
        rowIndex = writeDictionarySection(sheet, rowIndex + 1, "系列", seriesService.list(new QueryWrapper<Series>()
                .orderByAsc("id")).stream().map(item -> List.of(
                String.valueOf(item.getId()), item.getName(), item.getBrandId() == null ? "" : String.valueOf(item.getBrandId()))).toList());
        rowIndex = writeDictionarySection(sheet, rowIndex + 1, "分类", categoryService.list(new QueryWrapper<Category>()
                .orderByAsc("id")).stream().map(item -> List.of(
                String.valueOf(item.getId()), item.getName(), String.valueOf(item.getStatus()))).toList());
        rowIndex = writeDictionarySection(sheet, rowIndex + 1, "厂家", makerService.list(new QueryWrapper<Maker>()
                .orderByAsc("id")).stream().map(item -> List.of(
                String.valueOf(item.getId()), item.getName(), String.valueOf(item.getStatus()))).toList());
        writeDictionarySection(sheet, rowIndex + 1, "状态值", List.of(
                List.of("1", "有効", "normal"),
                List.of("0", "無効", "disabled")
        ));
        sheet.setColumnWidth(0, DICTIONARY_ID_COLUMN_WIDTH);
        sheet.setColumnWidth(1, DICTIONARY_NAME_COLUMN_WIDTH);
        sheet.setColumnWidth(2, DICTIONARY_ID_COLUMN_WIDTH);
    }

    private void buildValidationSheet(XSSFWorkbook workbook, TemplateScope scope) {
        Sheet sheet = workbook.createSheet(TEMPLATE_VALIDATION_SHEET_NAME);
        int rowIndex = 0;

        List<Brand> brands = brandService.list(new QueryWrapper<Brand>().orderByAsc("id"));
        List<Category> categories = categoryService.list(new QueryWrapper<Category>().orderByAsc("id"));
        List<Series> allSeries = seriesService.list(new QueryWrapper<Series>().orderByAsc("id"));
        List<Maker> allMakers = makerService.list(new QueryWrapper<Maker>().orderByAsc("id"));
        if (scope != null) {
            brands = scope.brands();
            categories = scope.categories();
            allSeries = scope.series();
            allMakers = scope.makers();
        }
        rowIndex = writeValidationBlock(sheet, rowIndex, "BRAND_ID", brands.stream()
                .map(item -> String.valueOf(item.getId()))
                .toList());
        rowIndex = writeValidationBlock(sheet, rowIndex, "BRAND_NAME", brands.stream()
                .map(Brand::getName)
                .toList());
        rowIndex = writeValidationBlock(sheet, rowIndex, "CATEGORY_ID", categories.stream()
                .map(item -> String.valueOf(item.getId()))
                .toList());
        rowIndex = writeValidationBlock(sheet, rowIndex, "CATEGORY_NAME", categories.stream()
                .map(Category::getName)
                .toList());
        rowIndex = writeValidationBlock(sheet, rowIndex, "HOT_FLAG", List.of("1", "0"));
        rowIndex = writeValidationBlock(sheet, rowIndex, "CURRENCY", List.of(CommonConstant.DEFAULT_CURRENCY_JPY));
        rowIndex = writeValidationBlock(sheet, rowIndex, "STATUS_FLAG", List.of("1", "0"));

        Map<Long, List<Series>> seriesByBrand = scope == null
                ? buildSeriesByBrandFromSeries(allSeries)
                : buildScopedSeriesByBrand(scope);

        for (Brand brand : brands) {
            Long brandId = brand.getId();
            List<Series> brandSeries = deduplicateSeries(seriesByBrand.getOrDefault(brandId, List.of()));
            List<Long> brandSeriesIds = brandSeries.stream().map(Series::getId).toList();
            List<Maker> brandMakers = deduplicateMakers((allMakers == null ? List.<Maker>of() : allMakers).stream()
                    .filter(item -> item.getSeriesId() != null && brandSeriesIds.contains(item.getSeriesId()))
                    .toList());
            rowIndex = writeValidationBlock(sheet, rowIndex, "SERIES_ID_" + brandId, brandSeries.stream()
                    .map(item -> String.valueOf(item.getId()))
                    .toList());
            rowIndex = writeValidationBlock(sheet, rowIndex, "SERIES_NAME_" + brandId, brandSeries.stream()
                    .map(Series::getName)
                    .toList());
            rowIndex = writeValidationBlock(sheet, rowIndex, "MAKER_ID_" + brandId, brandMakers.stream()
                    .map(item -> String.valueOf(item.getId()))
                    .toList());
            rowIndex = writeValidationBlock(sheet, rowIndex, "MAKER_NAME_" + brandId, brandMakers.stream()
                    .map(Maker::getName)
                    .toList());
        }

        workbook.setSheetHidden(workbook.getSheetIndex(sheet), true);
    }

    private Map<Long, List<Series>> buildSeriesByBrandFromSeries(List<Series> allSeries) {
        Map<Long, List<Series>> seriesByBrand = new LinkedHashMap<>();
        for (Series series : allSeries) {
            if (series == null || series.getBrandId() == null) {
                continue;
            }
            seriesByBrand.computeIfAbsent(series.getBrandId(), key -> new ArrayList<>()).add(series);
        }
        return seriesByBrand;
    }

    private Map<Long, List<Series>> buildScopedSeriesByBrand(TemplateScope scope) {
        Map<Long, Series> seriesById = new LinkedHashMap<>();
        for (Series series : scope.series()) {
            if (series != null && series.getId() != null) {
                seriesById.put(series.getId(), series);
            }
        }
        Map<Long, List<Series>> seriesByBrand = new LinkedHashMap<>();
        for (Goods goods : scope.scopedGoods()) {
            if (goods == null || goods.getBrandId() == null || goods.getSeriesId() == null) {
                continue;
            }
            Series series = seriesById.get(goods.getSeriesId());
            if (series == null) {
                continue;
            }
            seriesByBrand.computeIfAbsent(goods.getBrandId(), key -> new ArrayList<>()).add(series);
        }
        return seriesByBrand;
    }

    private void applyTemplateValidations(XSSFWorkbook workbook) {
        Sheet sheet = workbook.getSheet(TEMPLATE_SHEET_NAME);
        if (sheet == null) {
            return;
        }
        int firstRow = TEMPLATE_DATA_START_ROW_INDEX;
        int lastRow = TEMPLATE_VALIDATION_MAX_ROW;
        addExplicitListValidation(sheet, firstRow, lastRow, TEMPLATE_HEADERS.indexOf(TEMPLATE_HEADER_BRAND_NAME), "BRAND_NAME");
        addExplicitListValidation(sheet, firstRow, lastRow, TEMPLATE_HEADERS.indexOf(TEMPLATE_HEADER_CATEGORY_NAME), "CATEGORY_NAME");
        addExplicitListValidation(sheet, firstRow, lastRow, TEMPLATE_HEADERS.indexOf(TEMPLATE_HEADER_CURRENCY), "CURRENCY");
        addFormulaListValidation(sheet, firstRow, lastRow, TEMPLATE_HEADERS.indexOf(TEMPLATE_HEADER_SERIES_NAME),
                "IF($E%d=\"\",\"\",INDIRECT(\"SERIES_NAME_\"&$E%d))");
        addFormulaListValidation(sheet, firstRow, lastRow, TEMPLATE_HEADERS.indexOf(TEMPLATE_HEADER_MAKER_NAME),
                "IF($E%d=\"\",\"\",INDIRECT(\"MAKER_NAME_\"&$E%d))");
    }

    private int writeValidationBlock(Sheet sheet, int rowIndex, String rangeName, List<String> values) {
        Row headerRow = sheet.createRow(rowIndex);
        headerRow.createCell(0).setCellValue(rangeName);
        int startRow = rowIndex + 1;
        int currentRow = startRow;
        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }
            Row row = sheet.createRow(currentRow++);
            row.createCell(0).setCellValue(value);
        }
        if (currentRow == startRow) {
            Row row = sheet.createRow(currentRow++);
            row.createCell(0).setCellValue("__EMPTY__");
        }
        Name name = sheet.getWorkbook().createName();
        name.setNameName(rangeName);
        name.setRefersToFormula("'" + TEMPLATE_VALIDATION_SHEET_NAME + "'!$A$" + startRow + ":$A$" + (currentRow - 1));
        return currentRow + 1;
    }

    private void addExplicitListValidation(Sheet sheet, int firstRow, int lastRow, int columnIndex, String namedRange) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createFormulaListConstraint(namedRange);
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, columnIndex, columnIndex);
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(false);
        // Dropdowns are hints only: import can auto-create new brand/category values.
        validation.setShowErrorBox(false);
        sheet.addValidationData(validation);
    }

    private void addFormulaListValidation(Sheet sheet, int firstRow, int lastRow, int columnIndex, String formulaTemplate) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
            int excelRow = rowIndex + 1;
            String formula = String.format(Locale.ROOT, formulaTemplate, excelRow, excelRow);
            DataValidationConstraint constraint = helper.createFormulaListConstraint(formula);
            CellRangeAddressList addressList = new CellRangeAddressList(rowIndex, rowIndex, columnIndex, columnIndex);
            DataValidation validation = helper.createValidation(constraint, addressList);
            validation.setSuppressDropDownArrow(false);
            // Dropdowns are hints only: import can auto-create new series/maker values.
            validation.setShowErrorBox(false);
            sheet.addValidationData(validation);
        }
    }

    private List<Maker> deduplicateMakers(List<Maker> makers) {
        Map<Long, Maker> unique = new LinkedHashMap<>();
        for (Maker maker : makers) {
            if (maker != null && maker.getId() != null) {
                unique.put(maker.getId(), maker);
            }
        }
        return new ArrayList<>(unique.values());
    }

    private TemplateScope loadTemplateScope(GoodsQueryDTO scopeQuery) {
        QueryWrapper<Goods> wrapper = new QueryWrapper<Goods>()
                .select("brand_id", "series_id", "category_id", "maker_id");
        if (scopeQuery != null) {
            if (scopeQuery.getBrandId() != null) {
                wrapper.eq("brand_id", scopeQuery.getBrandId());
            }
            if (scopeQuery.getSeriesId() != null) {
                wrapper.eq("series_id", scopeQuery.getSeriesId());
            }
            if (scopeQuery.getCategoryId() != null) {
                wrapper.eq("category_id", scopeQuery.getCategoryId());
            }
            if (scopeQuery.getMakerId() != null) {
                wrapper.eq("maker_id", scopeQuery.getMakerId());
            }
        }
        List<Goods> scopedGoods = list(wrapper);
        List<Long> brandIds = distinctIds(scopedGoods.stream().map(Goods::getBrandId).toList());
        List<Long> seriesIds = distinctIds(scopedGoods.stream().map(Goods::getSeriesId).toList());
        List<Long> categoryIds = distinctIds(scopedGoods.stream().map(Goods::getCategoryId).toList());
        List<Long> makerIds = distinctIds(scopedGoods.stream().map(Goods::getMakerId).toList());
        return new TemplateScope(
                scopedGoods,
                loadBrandsByIds(brandIds),
                loadSeriesByIds(seriesIds),
                loadCategoriesByIds(categoryIds),
                loadMakersByIds(makerIds)
        );
    }

    private List<Long> distinctIds(List<Long> ids) {
        return ids == null ? List.of() : ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<Brand> loadBrandsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return brandService.list(new QueryWrapper<Brand>().in("id", ids).orderByAsc("id"));
    }

    private List<Series> loadSeriesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return seriesService.list(new QueryWrapper<Series>().in("id", ids).orderByAsc("id"));
    }

    private List<Category> loadCategoriesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return categoryService.list(new QueryWrapper<Category>().in("id", ids).orderByAsc("id"));
    }

    private List<Maker> loadMakersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return makerService.list(new QueryWrapper<Maker>().in("id", ids).orderByAsc("id"));
    }

    private List<Series> deduplicateSeries(List<Series> seriesList) {
        Map<Long, Series> unique = new LinkedHashMap<>();
        for (Series series : seriesList) {
            if (series != null && series.getId() != null) {
                unique.put(series.getId(), series);
            }
        }
        return new ArrayList<>(unique.values());
    }

    private int writeDictionarySection(Sheet sheet, int rowIndex, String title, List<List<String>> rows) {
        Row titleRow = sheet.createRow(rowIndex++);
        titleRow.createCell(0).setCellValue(title);
        Row headerRow = sheet.createRow(rowIndex++);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("名称");
        headerRow.createCell(2).setCellValue("扩展");
        for (List<String> rowValues : rows) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < rowValues.size(); i++) {
                row.createCell(i).setCellValue(rowValues.get(i));
            }
        }
        return rowIndex;
    }

    private Map<String, Integer> resolveHeaderIndexes(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> headerIndexes = new HashMap<>();
        for (Cell cell : headerRow) {
            String value = trimToNull(formatter.formatCellValue(cell));
            if (value != null) {
                headerIndexes.put(value, cell.getColumnIndex());
            }
        }
        for (String header : TEMPLATE_REQUIRED_HEADERS) {
            if (!headerIndexes.containsKey(header)) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_TEMPLATE_COLUMN_MISSING + header);
            }
        }
        return headerIndexes;
    }

    private boolean isTemplateDataRowEmpty(Row row, DataFormatter formatter, Map<String, Integer> headerIndexes) {
        for (String header : TEMPLATE_HEADERS) {
            Integer columnIndex = headerIndexes.get(header);
            if (columnIndex == null) {
                continue;
            }
            String value = trimToNull(formatter.formatCellValue(row.getCell(columnIndex)));
            if (value != null) {
                return false;
            }
        }
        return true;
    }

    private String readString(Row row, Integer columnIndex, DataFormatter formatter) {
        if (columnIndex == null) {
            return null;
        }
        return trimToNull(formatter.formatCellValue(row.getCell(columnIndex)));
    }

    private Long readLong(Row row, Integer columnIndex, DataFormatter formatter) {
        String value = readString(row, columnIndex, formatter);
        return value == null ? null : Long.valueOf(normalizeIntegerString(value));
    }

    private Integer readInteger(Row row, Integer columnIndex, DataFormatter formatter) {
        String value = readString(row, columnIndex, formatter);
        return value == null ? null : Integer.valueOf(normalizeIntegerString(value));
    }

    private BigDecimal readDecimal(Row row, Integer columnIndex, DataFormatter formatter) {
        String value = readString(row, columnIndex, formatter);
        return value == null ? null : new BigDecimal(value.replace(",", ""));
    }

    private LocalDateTime readDateTime(Row row, Integer columnIndex, DataFormatter formatter) {
        if (columnIndex == null) {
            return null;
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        if (DateUtil.isCellDateFormatted(cell)) {
            return Instant.ofEpochMilli(cell.getDateCellValue().getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }
        String value = trimToNull(formatter.formatCellValue(cell));
        if (value == null) {
            return null;
        }
        if (value.length() == 10) {
            return LocalDate.parse(value).atStartOfDay();
        }
        return LocalDateTime.parse(value, TEMPLATE_DATE_TIME_FORMATTER);
    }

    private Long resolveBrandId(GoodsBatchUpsertItemDTO item) {
        if (item.getBrandId() != null) {
            Brand brand = brandService.getByIdNotDeleted(item.getBrandId());
            if (brand == null) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                        rowMessage(item, "ブランドが見つかりません"));
            }
            return brand.getId();
        }
        String brandName = trimToNull(item.getBrandName());
        if (!StringUtils.hasText(brandName)) {
            return null;
        }
        Brand brand = brandService.getOne(new QueryWrapper<Brand>()
                .eq("name", brandName)
                .last("LIMIT 1"));
        if (brand == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, "ブランド名に該当するブランドが見つかりません"));
        }
        return brand.getId();
    }

    private Long resolveSeriesId(GoodsBatchUpsertItemDTO item, Long brandId) {
        if (item.getSeriesId() != null) {
            Series series = seriesService.getByIdNotDeleted(item.getSeriesId());
            if (series == null) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                        rowMessage(item, "シリーズが見つかりません"));
            }
            return series.getId();
        }
        String seriesName = trimToNull(item.getSeriesName());
        if (!StringUtils.hasText(seriesName)) {
            return null;
        }
        QueryWrapper<Series> wrapper = new QueryWrapper<Series>()
                .eq("name", seriesName)
                .last("LIMIT 1");
        if (brandId != null) {
            wrapper.eq("brand_id", brandId);
        }
        Series series = seriesService.getOne(wrapper);
        if (series == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, "シリーズ名に該当するシリーズが見つかりません"));
        }
        return series.getId();
    }

    private Long resolveCategoryId(GoodsBatchUpsertItemDTO item) {
        if (item.getCategoryId() != null) {
            Category category = categoryService.getByIdNotDeleted(item.getCategoryId());
            if (category == null) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                        rowMessage(item, "カテゴリが見つかりません"));
            }
            return category.getId();
        }
        String categoryName = trimToNull(item.getCategoryName());
        if (!StringUtils.hasText(categoryName)) {
            return null;
        }
        Category category = categoryService.getOne(new QueryWrapper<Category>()
                .eq("name", categoryName)
                .last("LIMIT 1"));
        if (category == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, "カテゴリ名に該当するカテゴリが見つかりません"));
        }
        return category.getId();
    }

    private Long resolveMakerId(GoodsBatchUpsertItemDTO item) {
        if (item.getMakerId() != null) {
            Maker maker = makerService.getByIdNotDeleted(item.getMakerId());
            if (maker == null) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                        rowMessage(item, "メーカーが見つかりません"));
            }
            return maker.getId();
        }
        String makerName = trimToNull(item.getMakerName());
        if (!StringUtils.hasText(makerName)) {
            return null;
        }
        Maker maker = makerService.getOne(new QueryWrapper<Maker>()
                .eq("name", makerName)
                .last("LIMIT 1"));
        if (maker == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, "メーカー名に該当するメーカーが見つかりません"));
        }
        return maker.getId();
    }

    private void validateItemWithinScope(GoodsBatchUpsertItemDTO item, GoodsQueryDTO scopeQuery) {
        if (!hasScopeFilter(scopeQuery)) {
            return;
        }
        ExistingGoodsTarget target = resolveExistingTarget(item);
        Goods targetGoods = target == null ? null : target.goods();
        Long brandId = firstNonNull(findExistingBrandId(item), targetGoods == null ? null : targetGoods.getBrandId());
        Long seriesId = firstNonNull(findExistingSeriesId(item, brandId), targetGoods == null ? null : targetGoods.getSeriesId());
        Long categoryId = firstNonNull(findExistingCategoryId(item), targetGoods == null ? null : targetGoods.getCategoryId());
        Long makerId = firstNonNull(findExistingMakerId(item, seriesId), targetGoods == null ? null : targetGoods.getMakerId());
        validateScopeMatch(item, "brand", scopeQuery.getBrandId(), brandId);
        validateScopeMatch(item, "series", scopeQuery.getSeriesId(), seriesId);
        validateScopeMatch(item, "category", scopeQuery.getCategoryId(), categoryId);
        validateScopeMatch(item, "maker", scopeQuery.getMakerId(), makerId);
    }

    private boolean hasScopeFilter(GoodsQueryDTO scopeQuery) {
        return scopeQuery != null
                && (scopeQuery.getBrandId() != null
                || scopeQuery.getSeriesId() != null
                || scopeQuery.getCategoryId() != null
                || scopeQuery.getMakerId() != null);
    }

    private void validateScopeMatch(GoodsBatchUpsertItemDTO item, String fieldName, Long expectedId, Long actualId) {
        if (expectedId == null) {
            return;
        }
        if (!Objects.equals(expectedId, actualId)) {
            throw new BusinessException(
                    MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, fieldName + " is outside selected filter scope")
            );
        }
    }

    private Long resolveOrCreateBrandId(GoodsBatchUpsertItemDTO item) {
        Long brandId = findExistingBrandId(item);
        if (brandId != null) {
            return brandId;
        }
        String brandName = trimToNull(item.getBrandName());
        if (!StringUtils.hasText(brandName)) {
            return null;
        }
        synchronized (masterDataLock("brand", brandName)) {
            brandId = findExistingBrandId(item);
            if (brandId != null) {
                return brandId;
            }
        Brand created = new Brand();
        created.setName(brandName);
        created.setEnglishName(brandName);
        created.setStatus(StatusEnum.NOMAL.getCode());
        try {
        if (!brandService.save(created)) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, "ブランドの作成に失敗しました"));
        }
        } catch (DuplicateKeyException ex) {
            Long existingId = findExistingBrandId(item);
            if (existingId != null) {
                return existingId;
            }
            throw ex;
        }
        return created.getId();
        }
    }

    private Long resolveOrCreateSeriesId(GoodsBatchUpsertItemDTO item, Long brandId) {
        Long seriesId = findExistingSeriesId(item, brandId);
        if (seriesId != null) {
            Series current = seriesService.getByIdNotDeleted(seriesId);
            return attachSeriesBrandIfNecessary(current, brandId).getId();
        }
        String seriesName = trimToNull(item.getSeriesName());
        if (!StringUtils.hasText(seriesName)) {
            return null;
        }
        synchronized (masterDataLock("series", String.valueOf(brandId), seriesName)) {
            seriesId = findExistingSeriesId(item, brandId);
            if (seriesId != null) {
                Series current = seriesService.getByIdNotDeleted(seriesId);
                return attachSeriesBrandIfNecessary(current, brandId).getId();
            }
        Series sameName = findAttachableSeriesByName(seriesName, brandId);
        if (sameName != null) {
            return attachSeriesBrandIfNecessary(sameName, brandId).getId();
        }
        Series created = new Series();
        created.setName(seriesName);
        created.setEnglishName(seriesName);
        created.setBrandId(brandId);
        created.setStatus(StatusEnum.NOMAL.getCode());
        try {
        if (!seriesService.save(created)) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, "シリーズの作成に失敗しました"));
        }
        } catch (DuplicateKeyException ex) {
            Long existingId = findExistingSeriesId(item, brandId);
            if (existingId != null) {
                return existingId;
            }
            throw ex;
        }
        return created.getId();
        }
    }

    private Long resolveOrCreateCategoryId(GoodsBatchUpsertItemDTO item) {
        Long categoryId = findExistingCategoryId(item);
        if (categoryId != null) {
            return categoryId;
        }
        String categoryName = trimToNull(item.getCategoryName());
        if (!StringUtils.hasText(categoryName)) {
            return null;
        }
        synchronized (masterDataLock("category", categoryName)) {
            categoryId = findExistingCategoryId(item);
            if (categoryId != null) {
                return categoryId;
            }
        Category created = new Category();
        created.setName(categoryName);
        created.setStatus(StatusEnum.NOMAL.getCode());
        try {
        if (!categoryService.save(created)) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, "分類の作成に失敗しました"));
        }
        } catch (DuplicateKeyException ex) {
            Long existingId = findExistingCategoryId(item);
            if (existingId != null) {
                return existingId;
            }
            throw ex;
        }
        return created.getId();
        }
    }

    private Long resolveOrCreateMakerId(GoodsBatchUpsertItemDTO item, Long seriesId) {
        Long makerId = findExistingMakerId(item, seriesId);
        if (makerId != null) {
            Maker current = makerService.getByIdNotDeleted(makerId);
            return attachMakerSeriesIfNecessary(current, seriesId).getId();
        }
        String makerName = trimToNull(item.getMakerName());
        if (!StringUtils.hasText(makerName)) {
            return null;
        }
        synchronized (masterDataLock("maker", String.valueOf(seriesId), makerName)) {
            makerId = findExistingMakerId(item, seriesId);
            if (makerId != null) {
                Maker current = makerService.getByIdNotDeleted(makerId);
                return attachMakerSeriesIfNecessary(current, seriesId).getId();
            }
        Maker sameName = findAttachableMakerByName(makerName, seriesId);
        if (sameName != null) {
            return attachMakerSeriesIfNecessary(sameName, seriesId).getId();
        }
        Maker created = new Maker();
        created.setName(makerName);
        created.setEnglishName(makerName);
        created.setSeriesId(seriesId);
        created.setStatus(StatusEnum.NOMAL.getCode());
        try {
        if (!makerService.save(created)) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    rowMessage(item, "メーカーの作成に失敗しました"));
        }
        } catch (DuplicateKeyException ex) {
            Long existingId = findExistingMakerId(item, seriesId);
            if (existingId != null) {
                return existingId;
            }
            throw ex;
        }
        return created.getId();
        }
    }

    private Series attachSeriesBrandIfNecessary(Series series, Long brandId) {
        if (series == null || brandId == null || Objects.equals(series.getBrandId(), brandId)) {
            return series;
        }
        if (series.getBrandId() == null) {
            series.setBrandId(brandId);
            seriesService.updateById(series);
        }
        return series;
    }

    private Maker attachMakerSeriesIfNecessary(Maker maker, Long seriesId) {
        if (maker == null || seriesId == null || Objects.equals(maker.getSeriesId(), seriesId)) {
            return maker;
        }
        if (maker.getSeriesId() == null) {
            maker.setSeriesId(seriesId);
            makerService.updateById(maker);
        }
        return maker;
    }

    private Long findExistingBrandId(GoodsBatchUpsertItemDTO item) {
        if (item.getBrandId() != null) {
            return item.getBrandId();
        }
        String brandName = trimToNull(item.getBrandName());
        if (!StringUtils.hasText(brandName)) {
            return null;
        }
        Brand brand = brandService.getOne(new QueryWrapper<Brand>().eq("name", brandName).last("LIMIT 1"));
        return brand == null ? null : brand.getId();
    }

    private Long findExistingSeriesId(GoodsBatchUpsertItemDTO item, Long brandId) {
        if (item.getSeriesId() != null) {
            return item.getSeriesId();
        }
        String seriesName = trimToNull(item.getSeriesName());
        if (!StringUtils.hasText(seriesName)) {
            return null;
        }
        QueryWrapper<Series> wrapper = new QueryWrapper<Series>().eq("name", seriesName);
        if (brandId != null) {
            wrapper.eq("brand_id", brandId);
        }
        Series series = seriesService.getOne(wrapper.last("LIMIT 1"));
        if (series != null) {
            return series.getId();
        }
        QueryWrapper<Series> sameNameWrapper = new QueryWrapper<Series>().eq("name", seriesName);
        if (brandId != null) {
            sameNameWrapper.isNull("brand_id");
        }
        Series sameName = seriesService.getOne(sameNameWrapper.last("LIMIT 1"));
        return canAttachSeriesToBrand(sameName, brandId) ? sameName.getId() : null;
    }

    private Long findExistingCategoryId(GoodsBatchUpsertItemDTO item) {
        if (item.getCategoryId() != null) {
            return item.getCategoryId();
        }
        String categoryName = trimToNull(item.getCategoryName());
        if (!StringUtils.hasText(categoryName)) {
            return null;
        }
        Category category = categoryService.getOne(new QueryWrapper<Category>().eq("name", categoryName).last("LIMIT 1"));
        return category == null ? null : category.getId();
    }

    private Long findExistingMakerId(GoodsBatchUpsertItemDTO item, Long seriesId) {
        if (item.getMakerId() != null) {
            return item.getMakerId();
        }
        String makerName = trimToNull(item.getMakerName());
        if (!StringUtils.hasText(makerName)) {
            return null;
        }
        QueryWrapper<Maker> wrapper = new QueryWrapper<Maker>().eq("name", makerName);
        if (seriesId != null) {
            wrapper.eq("series_id", seriesId);
        }
        Maker maker = makerService.getOne(wrapper.last("LIMIT 1"));
        if (maker != null) {
            return maker.getId();
        }
        QueryWrapper<Maker> sameNameWrapper = new QueryWrapper<Maker>().eq("name", makerName);
        if (seriesId != null) {
            sameNameWrapper.isNull("series_id");
        }
        Maker sameName = makerService.getOne(sameNameWrapper.last("LIMIT 1"));
        return canAttachMakerToSeries(sameName, seriesId) ? sameName.getId() : null;
    }

    private Series findAttachableSeriesByName(String seriesName, Long brandId) {
        QueryWrapper<Series> wrapper = new QueryWrapper<Series>().eq("name", seriesName);
        if (brandId != null) {
            wrapper.isNull("brand_id");
        }
        Series series = seriesService.getOne(wrapper.last("LIMIT 1"));
        return canAttachSeriesToBrand(series, brandId) ? series : null;
    }

    private Maker findAttachableMakerByName(String makerName, Long seriesId) {
        QueryWrapper<Maker> wrapper = new QueryWrapper<Maker>().eq("name", makerName);
        if (seriesId != null) {
            wrapper.isNull("series_id");
        }
        Maker maker = makerService.getOne(wrapper.last("LIMIT 1"));
        return canAttachMakerToSeries(maker, seriesId) ? maker : null;
    }

    private boolean canAttachSeriesToBrand(Series series, Long brandId) {
        return series != null
                && (brandId == null || series.getBrandId() == null || Objects.equals(series.getBrandId(), brandId));
    }

    private boolean canAttachMakerToSeries(Maker maker, Long seriesId) {
        return maker != null
                && (seriesId == null || maker.getSeriesId() == null || Objects.equals(maker.getSeriesId(), seriesId));
    }

    private Object masterDataLock(String type, String... parts) {
        StringBuilder key = new StringBuilder(type);
        if (parts != null) {
            for (String part : parts) {
                key.append(':').append(part == null ? "" : part.trim());
            }
        }
        return MASTER_DATA_LOCKS.computeIfAbsent(key.toString(), ignored -> new Object());
    }

    private StatusEnum parseStatus(String value) {
        return parseStatus(value, true);
    }

    private StatusEnum parseStatus(String value, boolean allowNull) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return allowNull ? null : null;
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        if ("1".equals(lower) || "normal".equals(lower) || "enabled".equals(lower)
                || "active".equals(lower) || "有効".equals(normalized) || "有效".equals(normalized)) {
            return StatusEnum.NOMAL;
        }
        if ("0".equals(lower) || "disabled".equals(lower) || "forbidden".equals(lower)
                || "inactive".equals(lower) || "無効".equals(normalized) || "无效".equals(normalized)) {
            return StatusEnum.FOBBIDEN;
        }
        throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "ステータス値が不正です: " + value);
    }

    private Integer parseFlag(String value) {
        return parseFlag(value, true);
    }

    private Integer parseFlag(String value, boolean allowNull) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return allowNull ? null : null;
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        if ("1".equals(lower) || "true".equals(lower) || "yes".equals(lower)
                || "y".equals(lower) || "是".equals(normalized)) {
            return 1;
        }
        if ("0".equals(lower) || "false".equals(lower) || "no".equals(lower)
                || "n".equals(lower) || "否".equals(normalized)) {
            return 0;
        }
        throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "フラグ値が不正です: " + value);
    }

    private LocalDateTime normalizePriceUpdateTime(BigDecimal updatePrice, LocalDateTime priceUpdateTime) {
        if (updatePrice != null && priceUpdateTime == null) {
            return LocalDateTime.now();
        }
        return priceUpdateTime;
    }

    private String normalizeIntegerString(String value) {
        String normalized = value.trim();
        if (normalized.endsWith(".0")) {
            normalized = normalized.substring(0, normalized.length() - 2);
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String resolveExceptionMessage(Exception ex) {
        Throwable current = ex;
        while (current.getCause() != null
                && current.getCause() != current
                && current instanceof BusinessException == false) {
            current = current.getCause();
        }
        return trimToNull(current.getMessage()) == null ? ERROR_UNKNOWN : current.getMessage();
    }

    private String rowMessage(GoodsBatchUpsertItemDTO item, String message) {
        return ROW_PREFIX + item.getRowNo() + ": " + message;
    }

    private record ExistingGoodsTarget(Goods goods, GoodsSku sku, GoodsImage image) {
    }

    private record PreparedBatchItem(
            GoodsBatchUpsertItemDTO item,
            String action,
            CreateGoodsDTO createDto,
            UpdateGoodsDTO updateDto) {
    }

    private record TemplateScope(
            List<Goods> scopedGoods,
            List<Brand> brands,
            List<Series> series,
            List<Category> categories,
            List<Maker> makers) {
    }

    private void validatePriceUpdateFields(java.math.BigDecimal updatePrice, LocalDateTime priceUpdateTime) {
        if (updatePrice != null && priceUpdateTime == null) {
            throw new co.handk.backend.exception.BusinessException(
                    MessageKeyConstant.ERROR_RUNTIME,
                    "updatePrice入力時はpriceUpdateTimeが必須です"
            );
        }
    }

    private String generateBarcode() {
        String ts = Long.toString(System.currentTimeMillis(), 36).toUpperCase();
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "BC" + ts + random;
    }


    @Override
    public PageResult<GoodsListVO> pageGoods(GoodsQueryDTO queryDTO) {
        Long total = baseMapper.countGoodsPage(queryDTO);
        if (total == null || total <= 0) {
            return PageResult.build(0L, queryDTO.getPageNum(), queryDTO.getPageSize(), Collections.emptyList());
        }
        long offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        List<GoodsListVO> records = baseMapper.selectGoodsPage(queryDTO, offset, queryDTO.getPageSize());
        return PageResult.build(total, queryDTO.getPageNum(), queryDTO.getPageSize(), records);
    }

    @Override
    public void exportGoods(GoodsQueryDTO query, HttpServletResponse response) {
        GoodsQueryDTO exportQuery = new GoodsQueryDTO();
        if (query != null) {
            BeanUtils.copyProperties(query, exportQuery);
        }
        exportQuery.setPageNum(1L);
        exportQuery.setPageSize(EXPORT_MAX_ROWS);

        List<GoodsListVO> records = pageGoods(exportQuery).getRecords();
        try (XSSFWorkbook workbook = buildBatchTemplateWorkbook(exportQuery)) {
            Sheet sheet = workbook.getSheet(TEMPLATE_SHEET_NAME);
            for (int i = 0; i < records.size(); i++) {
                Row row = sheet.getRow(TEMPLATE_DATA_START_ROW_INDEX + i);
                if (row == null) {
                    row = sheet.createRow(TEMPLATE_DATA_START_ROW_INDEX + i);
                }
                writeTemplateDataRow(row, toTemplateItem(records.get(i)));
            }
            writeWorkbookResponse(workbook, response, EXPORT_GOODS_FILE_NAME);
        } catch (IOException ex) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_EXPORT_FAILED, ex);
        }
    }

    private GoodsBatchUpsertItemDTO toTemplateItem(GoodsListVO record) {
        GoodsBatchUpsertItemDTO item = new GoodsBatchUpsertItemDTO();
        item.setGoodsId(record.getId());
        item.setSkuId(record.getSkuId());
        item.setName(record.getName());
        item.setEnglishName(record.getEnglishName());
        item.setBrandName(record.getBrandName());
        item.setSeriesName(record.getSeriesName());
        item.setCategoryName(record.getCategoryName());
        item.setMakerName(record.getMakerName());
        item.setDescription(record.getDescription());
        item.setIsHot(record.getIsHot() == null ? null : String.valueOf(record.getIsHot()));
        item.setSort(record.getSort());
        item.setSkuCode(record.getSkuCode());
        item.setSkuName(record.getSkuName());
        item.setPrice(record.getPrice());
        item.setCurrency(record.getCurrency());
        item.setCostPrice(record.getCostPrice());
        item.setUpdatePrice(record.getUpdatePrice());
        item.setPriceUpdateTime(record.getPriceUpdateTime());
        item.setBarcode(record.getBarcode());
        item.setWeight(record.getWeight());
        item.setVolume(record.getVolume());
        item.setSkuStatus(record.getSkuStatus() == null ? null : String.valueOf(record.getSkuStatus()));
        item.setImageId(record.getImageId());
        item.setImageUrl(StringUtils.hasText(record.getImageUrl())
                ? fileStorageService.toApiPath(UploadBizType.GOODS, record.getImageUrl())
                : null);
        item.setImageSort(record.getImageSort());
        item.setStatus(record.getStatus() == null ? null : String.valueOf(record.getStatus()));
        return item;
    }

    private void writeTemplateDataRow(Row row, GoodsBatchUpsertItemDTO item) {
        int column = 0;
        writeCell(row, column++, item.getGoodsId());
        writeCell(row, column++, item.getSkuId());
        writeCell(row, column++, item.getName());
        writeCell(row, column++, item.getEnglishName());
        writeCell(row, column++, item.getBrandName());
        writeCell(row, column++, item.getSeriesName());
        writeCell(row, column++, item.getCategoryName());
        writeCell(row, column++, item.getMakerName());
        writeCell(row, column++, item.getSkuCode());
        writeCell(row, column++, item.getSkuName());
        writeCell(row, column++, item.getPrice());
        writeCell(row, column, item.getCurrency());
    }

    private void writeCell(Row row, int columnIndex, Object value) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = row.createCell(columnIndex);
        }
        if (value == null) {
            cell.setBlank();
            return;
        }
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
            return;
        }
        if (value instanceof LocalDateTime dateTime) {
            cell.setCellValue(TEMPLATE_DATE_TIME_FORMATTER.format(dateTime));
            return;
        }
        cell.setCellValue(String.valueOf(value));
    }

    private void attachImportErrorReport(String originalFileName, byte[] fileBytes, GoodsBatchUpsertResultVO result) {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(fileBytes));
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.getSheet(TEMPLATE_SHEET_NAME);
            if (sheet == null) {
                sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            }
            if (sheet == null) {
                return;
            }
            annotateImportResultSheet(workbook, sheet, result);
            workbook.write(outputStream);
            result.setErrorReportBase64(Base64.getEncoder().encodeToString(outputStream.toByteArray()));
            result.setErrorReportFileName(resolveImportResultFileName(originalFileName, workbook));
        } catch (Exception ex) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_EXPORT_FAILED, ex);
        }
    }

    private void annotateImportResultSheet(Workbook workbook, Sheet sheet, GoodsBatchUpsertResultVO result) {
        Row headerRow = sheet.getRow(TEMPLATE_HEADER_ROW_INDEX);
        if (headerRow == null) {
            headerRow = sheet.createRow(TEMPLATE_HEADER_ROW_INDEX);
        }
        int actionColumnIndex = TEMPLATE_HEADERS.size();
        int messageColumnIndex = TEMPLATE_HEADERS.size() + 1;
        headerRow.createCell(actionColumnIndex).setCellValue(IMPORT_RESULT_ACTION_HEADER);
        headerRow.createCell(messageColumnIndex).setCellValue(IMPORT_RESULT_MESSAGE_HEADER);

        CellStyle failedStyle = workbook.createCellStyle();
        failedStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        failedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (GoodsBatchUpsertRowResultVO rowResult : result.getRows()) {
            if (rowResult.getRowNo() == null) {
                continue;
            }
            Row row = sheet.getRow(rowResult.getRowNo() - 1);
            if (row == null) {
                row = sheet.createRow(rowResult.getRowNo() - 1);
            }
            writeCell(row, actionColumnIndex, rowResult.getAction());
            writeCell(row, messageColumnIndex, rowResult.getMessage());
            if (Boolean.FALSE.equals(rowResult.getSuccess())) {
                int cellCount = Math.max(messageColumnIndex + 1, row.getLastCellNum());
                for (int columnIndex = 0; columnIndex < cellCount; columnIndex++) {
                    Cell cell = row.getCell(columnIndex);
                    if (cell == null) {
                        cell = row.createCell(columnIndex);
                    }
                    cell.setCellStyle(failedStyle);
                }
            }
        }
    }

    private String resolveImportResultFileName(String originalFileName, Workbook workbook) {
        String defaultExtension = workbook instanceof XSSFWorkbook ? ".xlsx" : ".xls";
        if (!StringUtils.hasText(originalFileName)) {
            return "goods_import_result" + defaultExtension;
        }
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return originalFileName + "_result" + defaultExtension;
        }
        return originalFileName.substring(0, dotIndex) + "_result" + originalFileName.substring(dotIndex);
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
    protected GoodsVO toVO(Goods entity) {
        if (entity == null) {
            return null;
        }
        GoodsVO vo = new GoodsVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> Goods toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Goods entity = new Goods();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
