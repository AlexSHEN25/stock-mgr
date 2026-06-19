package co.handk.backend.service.impl;

import co.handk.backend.entity.Goods;
import co.handk.backend.entity.GoodsImage;
import co.handk.backend.entity.GoodsSku;
import co.handk.backend.entity.Brand;
import co.handk.backend.entity.Category;
import co.handk.backend.entity.Maker;
import co.handk.backend.entity.BrandMakerRelation;
import co.handk.backend.entity.Series;
import co.handk.backend.entity.SeriesBrandRelation;
import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.mapper.GoodsMapper;
import co.handk.backend.mapper.BrandMakerRelationMapper;
import co.handk.backend.mapper.SeriesBrandRelationMapper;
import co.handk.backend.constant.UploadBizType;
import co.handk.backend.service.BrandService;
import co.handk.backend.service.CategoryService;
import co.handk.backend.service.FileStorageService;
import co.handk.backend.service.BrandMakerRelationService;
import co.handk.backend.service.GoodsImageService;
import co.handk.backend.service.GoodsService;
import co.handk.backend.service.GoodsSkuService;
import co.handk.backend.service.MakerService;
import co.handk.backend.service.SeriesService;
import co.handk.backend.service.SeriesBrandRelationService;
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
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoodsServiceImpl extends BaseServiceImpl<GoodsMapper, Goods, GoodsVO>
        implements GoodsService {

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
    private static final long EXPORT_MAX_ROWS = 10_000L;
    private static final String EXPORT_GOODS_FILE_NAME = "goods_export.xlsx";
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
    private static final List<String> TEMPLATE_HEADERS = List.of(
            HEADER_GOODS_ID,
            HEADER_SKU_ID,
            HEADER_GOODS_NAME,
            HEADER_ENGLISH_NAME,
            HEADER_BRAND_ID,
            HEADER_BRAND_NAME,
            HEADER_SERIES_ID,
            HEADER_SERIES_NAME,
            HEADER_CATEGORY_ID,
            HEADER_CATEGORY_NAME,
            HEADER_MAKER_ID,
            HEADER_MAKER_NAME,
            HEADER_DESCRIPTION,
            HEADER_IS_HOT,
            HEADER_SORT,
            HEADER_SKU_CODE,
            HEADER_SKU_NAME,
            HEADER_PRICE,
            HEADER_CURRENCY,
            HEADER_COST_PRICE,
            HEADER_UPDATE_PRICE,
            HEADER_PRICE_UPDATE_TIME,
            HEADER_BARCODE,
            HEADER_WEIGHT,
            HEADER_VOLUME,
            HEADER_SKU_STATUS,
            HEADER_IMAGE_ID,
            HEADER_IMAGE_URL,
            HEADER_IMAGE_SORT,
            HEADER_GOODS_STATUS
    );

    private final GoodsSkuService goodsSkuService;
    private final GoodsImageService goodsImageService;
    private final FileStorageService fileStorageService;
    private final BrandService brandService;
    private final SeriesService seriesService;
    private final CategoryService categoryService;
    private final MakerService makerService;
    private final BrandMakerRelationService brandMakerRelationService;
    private final SeriesBrandRelationService seriesBrandRelationService;
    private final BrandMakerRelationMapper brandMakerRelationMapper;
    private final SeriesBrandRelationMapper seriesBrandRelationMapper;
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
            String normalizedImageUrl = fileStorageService.normalize(UploadBizType.GOODS, dto.getImageUrl());
            UpdateWrapper<GoodsImage> imageWrapper = new UpdateWrapper<GoodsImage>()
                    .eq("goods_id", dto.getId())
                    .set(StringUtils.hasText(normalizedImageUrl), "image_url", normalizedImageUrl)
                    .set(dto.getImageSort() != null, "sort", dto.getImageSort())
                    .set(StringUtils.hasText(dto.getSkuCode()), "sku_code", dto.getSkuCode());
            if (dto.getImageId() != null) {
                imageWrapper.eq("id", dto.getImageId());
            }
            boolean imageUpdated = goodsImageService.update(null, imageWrapper);
            if (!imageUpdated) {
                throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "商品画像の更新に失敗しました");
            }
        }
        return true;
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
        if (file == null || file.isEmpty()) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_IMPORT_FILE_REQUIRED);
        }
        return batchUpsertItems(parseGoodsImportFile(file));
    }

    @Override
    public void downloadBatchTemplate(HttpServletResponse response) {
        try (XSSFWorkbook workbook = buildBatchTemplateWorkbook()) {
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
        if (brandId != null && seriesId != null) {
            seriesBrandRelationMapper.upsertRelation(seriesId, brandId, co.handk.backend.annotation.context.UserContext.getUserIdOrDefault());
        }
        if (brandId != null && makerId != null) {
            brandMakerRelationMapper.upsertRelation(brandId, makerId, co.handk.backend.annotation.context.UserContext.getUserIdOrDefault());
        }
    }

    private void cleanupCascadingRelations(Long brandId, Long seriesId, Long makerId) {
        if (brandId != null && seriesId != null && !existsGoodsWithSeriesBrand(brandId, seriesId)) {
            seriesBrandRelationMapper.update(null, new UpdateWrapper<SeriesBrandRelation>()
                    .eq("brand_id", brandId)
                    .eq("series_id", seriesId)
                    .set("deleted", DeleteEnum.DELETED.getCode()));
        }
        if (brandId != null && makerId != null && !existsGoodsWithBrandMaker(brandId, makerId)) {
            brandMakerRelationMapper.update(null, new UpdateWrapper<BrandMakerRelation>()
                    .eq("brand_id", brandId)
                    .eq("maker_id", makerId)
                    .set("deleted", DeleteEnum.DELETED.getCode()));
        }
    }

    private boolean existsGoodsWithSeriesBrand(Long brandId, Long seriesId) {
        return this.count(new QueryWrapper<Goods>()
                .eq("brand_id", brandId)
                .eq("series_id", seriesId)) > 0;
    }

    private boolean existsGoodsWithBrandMaker(Long brandId, Long makerId) {
        return this.count(new QueryWrapper<Goods>()
                .eq("brand_id", brandId)
                .eq("maker_id", makerId)) > 0;
    }

    private GoodsBatchUpsertResultVO batchUpsertItems(List<GoodsBatchUpsertItemDTO> items) {
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

        for (int index = 0; index < safeItems.size(); index++) {
            GoodsBatchUpsertItemDTO item = safeItems.get(index);
            int rowNo = item.getRowNo() == null ? index + 1 : item.getRowNo();
            item.setRowNo(rowNo);
            GoodsBatchUpsertRowResultVO rowResult = new GoodsBatchUpsertRowResultVO();
            rowResult.setRowNo(rowNo);
            rowResult.setSkuCode(trimToNull(item.getSkuCode()));
            try {
                GoodsBatchUpsertRowResultVO committed = executeBatchRowInNewTransaction(item);
                rowResult.setSuccess(true);
                rowResult.setAction(committed.getAction());
                rowResult.setGoodsId(committed.getGoodsId());
                rowResult.setSkuId(committed.getSkuId());
                rowResult.setMessage(committed.getMessage());
                result.setSuccessCount(result.getSuccessCount() + 1);
                if (GoodsBatchActionEnum.CREATED.getCode().equals(committed.getAction())) {
                    result.setCreatedCount(result.getCreatedCount() + 1);
                } else {
                    result.setUpdatedCount(result.getUpdatedCount() + 1);
                }
            } catch (Exception ex) {
                rowResult.setSuccess(false);
                rowResult.setAction(GoodsBatchActionEnum.FAILED.getCode());
                rowResult.setMessage(resolveExceptionMessage(ex));
                result.setFailureCount(result.getFailureCount() + 1);
            }
            result.getRows().add(rowResult);
        }
        return result;
    }

    private GoodsService goodsServiceProxy() {
        return applicationContext.getBean(GoodsService.class);
    }

    private GoodsBatchUpsertRowResultVO executeBatchRowInNewTransaction(GoodsBatchUpsertItemDTO item) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template.execute(status -> upsertSingleItem(item));
    }

    private GoodsBatchUpsertRowResultVO upsertSingleItem(GoodsBatchUpsertItemDTO item) {
        GoodsBatchUpsertRowResultVO rowResult = new GoodsBatchUpsertRowResultVO();
        rowResult.setRowNo(item.getRowNo());
        rowResult.setSkuCode(trimToNull(item.getSkuCode()));
        ExistingGoodsTarget target = resolveExistingTarget(item);
        if (target == null) {
            CreateGoodsDTO createDto = buildCreateGoodsDto(item);
            if (!goodsServiceProxy().saveGoods(createDto)) {
                throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_CREATE_GOODS_FAILED);
            }
            ExistingGoodsTarget saved = resolveExistingTarget(item);
            rowResult.setAction(GoodsBatchActionEnum.CREATED.getCode());
            rowResult.setGoodsId(saved == null || saved.goods() == null ? null : saved.goods().getId());
            rowResult.setSkuId(saved == null || saved.sku() == null ? null : saved.sku().getId());
            rowResult.setMessage(GoodsBatchActionEnum.CREATED.getMessage());
            return rowResult;
        }

        UpdateGoodsDTO updateDto = buildUpdateGoodsDto(item, target);
        if (!goodsServiceProxy().updateGoods(updateDto)) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_UPDATE_GOODS_FAILED);
        }
        ExistingGoodsTarget updated = resolveExistingTarget(item);
        rowResult.setAction(GoodsBatchActionEnum.UPDATED.getCode());
        rowResult.setGoodsId(updated == null || updated.goods() == null ? null : updated.goods().getId());
        rowResult.setSkuId(updated == null || updated.sku() == null ? null : updated.sku().getId());
        rowResult.setMessage(GoodsBatchActionEnum.UPDATED.getMessage());
        return rowResult;
    }

    private CreateGoodsDTO buildCreateGoodsDto(GoodsBatchUpsertItemDTO item) {
        CreateGoodsDTO dto = new CreateGoodsDTO();
        Long brandId = resolveBrandId(item);
        Long categoryId = resolveCategoryId(item);
        Long seriesId = resolveSeriesId(item, brandId);
        Long makerId = resolveMakerId(item);
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
        Long brandId = firstNonNull(resolveBrandId(item), goods.getBrandId());
        Long categoryId = firstNonNull(resolveCategoryId(item), goods.getCategoryId());
        dto.setBrandId(brandId);
        dto.setSeriesId(firstNonNull(resolveSeriesId(item, brandId), goods.getSeriesId()));
        dto.setCategoryId(categoryId);
        dto.setMakerId(firstNonNull(resolveMakerId(item), goods.getMakerId()));
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
                item.setGoodsId(readLong(row, headerIndexes.get(HEADER_GOODS_ID), formatter));
                item.setSkuId(readLong(row, headerIndexes.get(HEADER_SKU_ID), formatter));
                item.setName(readString(row, headerIndexes.get(HEADER_GOODS_NAME), formatter));
                item.setEnglishName(readString(row, headerIndexes.get(HEADER_ENGLISH_NAME), formatter));
                item.setBrandId(readLong(row, headerIndexes.get(HEADER_BRAND_ID), formatter));
                item.setBrandName(readString(row, headerIndexes.get(HEADER_BRAND_NAME), formatter));
                item.setSeriesId(readLong(row, headerIndexes.get(HEADER_SERIES_ID), formatter));
                item.setSeriesName(readString(row, headerIndexes.get(HEADER_SERIES_NAME), formatter));
                item.setCategoryId(readLong(row, headerIndexes.get(HEADER_CATEGORY_ID), formatter));
                item.setCategoryName(readString(row, headerIndexes.get(HEADER_CATEGORY_NAME), formatter));
                item.setMakerId(readLong(row, headerIndexes.get(HEADER_MAKER_ID), formatter));
                item.setMakerName(readString(row, headerIndexes.get(HEADER_MAKER_NAME), formatter));
                item.setDescription(readString(row, headerIndexes.get(HEADER_DESCRIPTION), formatter));
                item.setIsHot(readString(row, headerIndexes.get(HEADER_IS_HOT), formatter));
                item.setSort(readInteger(row, headerIndexes.get(HEADER_SORT), formatter));
                item.setSkuCode(readString(row, headerIndexes.get(HEADER_SKU_CODE), formatter));
                item.setSkuName(readString(row, headerIndexes.get(HEADER_SKU_NAME), formatter));
                item.setPrice(readDecimal(row, headerIndexes.get(HEADER_PRICE), formatter));
                item.setCurrency(readString(row, headerIndexes.get(HEADER_CURRENCY), formatter));
                item.setCostPrice(readDecimal(row, headerIndexes.get(HEADER_COST_PRICE), formatter));
                item.setUpdatePrice(readDecimal(row, headerIndexes.get(HEADER_UPDATE_PRICE), formatter));
                item.setPriceUpdateTime(readDateTime(row, headerIndexes.get(HEADER_PRICE_UPDATE_TIME), formatter));
                item.setBarcode(readString(row, headerIndexes.get(HEADER_BARCODE), formatter));
                item.setWeight(readDecimal(row, headerIndexes.get(HEADER_WEIGHT), formatter));
                item.setVolume(readDecimal(row, headerIndexes.get(HEADER_VOLUME), formatter));
                item.setSkuStatus(readString(row, headerIndexes.get(HEADER_SKU_STATUS), formatter));
                item.setImageId(readLong(row, headerIndexes.get(HEADER_IMAGE_ID), formatter));
                item.setImageUrl(readString(row, headerIndexes.get(HEADER_IMAGE_URL), formatter));
                item.setImageSort(readInteger(row, headerIndexes.get(HEADER_IMAGE_SORT), formatter));
                item.setStatus(readString(row, headerIndexes.get(HEADER_GOODS_STATUS), formatter));
                items.add(item);
            }
            return items;
        } catch (IOException ex) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_IMPORT_FILE_READ_FAILED, ex);
        }
    }

    private XSSFWorkbook buildBatchTemplateWorkbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        buildTemplateSheet(workbook);
        buildInstructionSheet(workbook);
        buildDictionarySheet(workbook);
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
        sheet.setColumnWidth(0, INSTRUCTION_COLUMN_WIDTH);
    }

    private void buildDictionarySheet(XSSFWorkbook workbook) {
        Sheet sheet = workbook.createSheet(DICTIONARY_SHEET_NAME);
        int rowIndex = 0;
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
        for (String header : TEMPLATE_HEADERS) {
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
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(EXPORT_GOODS_SHEET_NAME);
            CellStyle headerStyle = createExportHeaderStyle(workbook);
            writeExportHeader(sheet, headerStyle, GOODS_EXPORT_HEADERS);
            for (int i = 0; i < records.size(); i++) {
                GoodsListVO item = records.get(i);
                Row row = sheet.createRow(i + 1);
                int column = 0;
                writeExportCell(row, column++, item.getSkuId());
                writeExportCell(row, column++, item.getName());
                writeExportCell(row, column++, item.getEnglishName());
                writeExportCell(row, column++, item.getBrandName());
                writeExportCell(row, column++, item.getSeriesName());
                writeExportCell(row, column++, item.getCategoryName());
                writeExportCell(row, column++, item.getMakerName());
                writeExportCell(row, column++, item.getSkuCode());
                writeExportCell(row, column++, item.getSkuName());
                writeExportCell(row, column++, item.getPrice());
                writeExportCell(row, column++, item.getCurrency());
                writeExportCell(row, column++, item.getStatusDesc());
                writeExportCell(row, column++, item.getDescription());
                writeExportCell(row, column, item.getSort());
            }
            writeWorkbookResponse(workbook, response, EXPORT_GOODS_FILE_NAME);
        } catch (IOException ex) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, ERROR_EXPORT_FAILED, ex);
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
            sheet.setColumnWidth(i, EXCEL_COLUMN_WIDTH);
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
            cell.setCellValue(TEMPLATE_DATE_TIME_FORMATTER.format(dateTime));
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
