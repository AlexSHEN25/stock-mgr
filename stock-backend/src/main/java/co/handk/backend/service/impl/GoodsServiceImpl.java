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
import co.handk.common.constant.NumberConstant;
import co.handk.common.enums.DeleteEnum;
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

    private static final String TEMPLATE_SHEET_NAME = "商品导入模板";
    private static final String INSTRUCTION_SHEET_NAME = "填写说明";
    private static final String DICTIONARY_SHEET_NAME = "基础数据";
    private static final int TEMPLATE_HEADER_ROW_INDEX = 0;
    private static final int TEMPLATE_NOTE_ROW_INDEX = 1;
    private static final int TEMPLATE_DATA_START_ROW_INDEX = 2;
    private static final DateTimeFormatter TEMPLATE_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<String> TEMPLATE_HEADERS = List.of(
            "商品ID",
            "SKU ID",
            "商品名称",
            "英文名称",
            "品牌ID",
            "品牌名称",
            "系列ID",
            "系列名称",
            "分类ID",
            "分类名称",
            "厂家ID",
            "厂家名称",
            "说明",
            "热门",
            "排序",
            "SKU编码",
            "SKU名称",
            "价格",
            "货币",
            "成本价",
            "调价",
            "调价时间",
            "条码",
            "重量",
            "体积",
            "SKU状态",
            "图片ID",
            "图片URL",
            "图片排序",
            "商品状态"
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
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "import file is required");
        }
        return batchUpsertItems(parseGoodsImportFile(file));
    }

    @Override
    public void downloadBatchTemplate(HttpServletResponse response) {
        try (XSSFWorkbook workbook = buildBatchTemplateWorkbook()) {
            String fileName = "goods_batch_template.xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            workbook.write(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ex) {
            throw new BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "failed to generate goods import template",
                    ex
            );
        }
    }

    private void syncCascadingRelations(Long brandId, Long seriesId, Long makerId) {
        if (brandId != null && seriesId != null) {
            SeriesBrandRelation seriesRelation = seriesBrandRelationService.getOne(new QueryWrapper<SeriesBrandRelation>()
                    .eq("brand_id", brandId)
                    .eq("series_id", seriesId)
                    .last("LIMIT 1"));
            if (seriesRelation == null) {
                seriesRelation = new SeriesBrandRelation();
                seriesRelation.setBrandId(brandId);
                seriesRelation.setSeriesId(seriesId);
                seriesBrandRelationService.save(seriesRelation);
            }
        }
        if (brandId != null && makerId != null) {
            BrandMakerRelation makerRelation = brandMakerRelationService.getOne(new QueryWrapper<BrandMakerRelation>()
                    .eq("brand_id", brandId)
                    .eq("maker_id", makerId)
                    .last("LIMIT 1"));
            if (makerRelation == null) {
                makerRelation = new BrandMakerRelation();
                makerRelation.setBrandId(brandId);
                makerRelation.setMakerId(makerId);
                brandMakerRelationService.save(makerRelation);
            }
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
                if ("CREATED".equals(committed.getAction())) {
                    result.setCreatedCount(result.getCreatedCount() + 1);
                } else {
                    result.setUpdatedCount(result.getUpdatedCount() + 1);
                }
            } catch (Exception ex) {
                rowResult.setSuccess(false);
                rowResult.setAction("FAILED");
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
                throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "failed to create goods");
            }
            ExistingGoodsTarget saved = resolveExistingTarget(item);
            rowResult.setAction("CREATED");
            rowResult.setGoodsId(saved == null || saved.goods() == null ? null : saved.goods().getId());
            rowResult.setSkuId(saved == null || saved.sku() == null ? null : saved.sku().getId());
            rowResult.setMessage("created");
            return rowResult;
        }

        UpdateGoodsDTO updateDto = buildUpdateGoodsDto(item, target);
        if (!goodsServiceProxy().updateGoods(updateDto)) {
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "failed to update goods");
        }
        ExistingGoodsTarget updated = resolveExistingTarget(item);
        rowResult.setAction("UPDATED");
        rowResult.setGoodsId(updated == null || updated.goods() == null ? null : updated.goods().getId());
        rowResult.setSkuId(updated == null || updated.sku() == null ? null : updated.sku().getId());
        rowResult.setMessage("updated");
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
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "row " + item.getRowNo() + ": 商品名称 or SKU编码 is required");
        }
        if (brandId == null) {
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "row " + item.getRowNo() + ": brand is required");
        }
        if (categoryId == null) {
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "row " + item.getRowNo() + ": category is required");
        }
        if (!StringUtils.hasText(skuCode)) {
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "row " + item.getRowNo() + ": SKU编码 is required");
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
        Long brandId = firstNonNull(resolveBrandId(item, false), goods.getBrandId());
        Long categoryId = firstNonNull(resolveCategoryId(item, false), goods.getCategoryId());
        dto.setBrandId(brandId);
        dto.setSeriesId(firstNonNull(resolveSeriesId(item, brandId, false), goods.getSeriesId()));
        dto.setCategoryId(categoryId);
        dto.setMakerId(firstNonNull(resolveMakerId(item, false), goods.getMakerId()));
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
                throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "row " + item.getRowNo() + ": goods not found by goodsId");
            }
        }
        if (item.getSkuId() != null) {
            sku = goodsSkuService.getByIdNotDeleted(item.getSkuId());
            if (sku == null) {
                throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "row " + item.getRowNo() + ": sku not found by skuId");
            }
            Goods skuGoods = this.getByIdNotDeleted(sku.getGoodsId());
            if (goods != null && !goods.getId().equals(skuGoods == null ? null : skuGoods.getId())) {
                throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "row " + item.getRowNo() + ": goodsId and skuId do not match");
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
                throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "template sheet not found");
            }
            DataFormatter formatter = new DataFormatter();
            Row headerRow = sheet.getRow(TEMPLATE_HEADER_ROW_INDEX);
            if (headerRow == null) {
                throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "header row is missing");
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
                item.setGoodsId(readLong(row, headerIndexes.get("商品ID"), formatter));
                item.setSkuId(readLong(row, headerIndexes.get("SKU ID"), formatter));
                item.setName(readString(row, headerIndexes.get("商品名称"), formatter));
                item.setEnglishName(readString(row, headerIndexes.get("英文名称"), formatter));
                item.setBrandId(readLong(row, headerIndexes.get("品牌ID"), formatter));
                item.setBrandName(readString(row, headerIndexes.get("品牌名称"), formatter));
                item.setSeriesId(readLong(row, headerIndexes.get("系列ID"), formatter));
                item.setSeriesName(readString(row, headerIndexes.get("系列名称"), formatter));
                item.setCategoryId(readLong(row, headerIndexes.get("分类ID"), formatter));
                item.setCategoryName(readString(row, headerIndexes.get("分类名称"), formatter));
                item.setMakerId(readLong(row, headerIndexes.get("厂家ID"), formatter));
                item.setMakerName(readString(row, headerIndexes.get("厂家名称"), formatter));
                item.setDescription(readString(row, headerIndexes.get("说明"), formatter));
                item.setIsHot(readString(row, headerIndexes.get("热门"), formatter));
                item.setSort(readInteger(row, headerIndexes.get("排序"), formatter));
                item.setSkuCode(readString(row, headerIndexes.get("SKU编码"), formatter));
                item.setSkuName(readString(row, headerIndexes.get("SKU名称"), formatter));
                item.setPrice(readDecimal(row, headerIndexes.get("价格"), formatter));
                item.setCurrency(readString(row, headerIndexes.get("货币"), formatter));
                item.setCostPrice(readDecimal(row, headerIndexes.get("成本价"), formatter));
                item.setUpdatePrice(readDecimal(row, headerIndexes.get("调价"), formatter));
                item.setPriceUpdateTime(readDateTime(row, headerIndexes.get("调价时间"), formatter));
                item.setBarcode(readString(row, headerIndexes.get("条码"), formatter));
                item.setWeight(readDecimal(row, headerIndexes.get("重量"), formatter));
                item.setVolume(readDecimal(row, headerIndexes.get("体积"), formatter));
                item.setSkuStatus(readString(row, headerIndexes.get("SKU状态"), formatter));
                item.setImageId(readLong(row, headerIndexes.get("图片ID"), formatter));
                item.setImageUrl(readString(row, headerIndexes.get("图片URL"), formatter));
                item.setImageSort(readInteger(row, headerIndexes.get("图片排序"), formatter));
                item.setStatus(readString(row, headerIndexes.get("商品状态"), formatter));
                items.add(item);
            }
            return items;
        } catch (IOException ex) {
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "failed to read import file", ex);
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
            sheet.setColumnWidth(i, 18 * 256);
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
        sheet.setColumnWidth(0, 90 * 256);
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
        sheet.setColumnWidth(0, 20 * 256);
        sheet.setColumnWidth(1, 28 * 256);
        sheet.setColumnWidth(2, 20 * 256);
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
                throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "missing template column: " + header);
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
        return resolveBrandId(item, true);
    }

    private Long resolveBrandId(GoodsBatchUpsertItemDTO item, boolean failWhenMissingName) {
        if (item.getBrandId() != null) {
            Brand brand = brandService.getByIdNotDeleted(item.getBrandId());
            if (brand == null) {
                throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "row " + item.getRowNo() + ": brand not found");
            }
            return brand.getId();
        }
        String brandName = trimToNull(item.getBrandName());
        if (!StringUtils.hasText(brandName)) {
            return failWhenMissingName ? null : null;
        }
        Brand brand = brandService.getOne(new QueryWrapper<Brand>()
                .eq("name", brandName)
                .last("LIMIT 1"));
        if (brand == null) {
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "row " + item.getRowNo() + ": brand not found by name");
        }
        return brand.getId();
    }

    private Long resolveSeriesId(GoodsBatchUpsertItemDTO item, Long brandId) {
        return resolveSeriesId(item, brandId, true);
    }

    private Long resolveSeriesId(GoodsBatchUpsertItemDTO item, Long brandId, boolean failWhenMissingName) {
        if (item.getSeriesId() != null) {
            Series series = seriesService.getByIdNotDeleted(item.getSeriesId());
            if (series == null) {
                throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "row " + item.getRowNo() + ": series not found");
            }
            return series.getId();
        }
        String seriesName = trimToNull(item.getSeriesName());
        if (!StringUtils.hasText(seriesName)) {
            return failWhenMissingName ? null : null;
        }
        QueryWrapper<Series> wrapper = new QueryWrapper<Series>()
                .eq("name", seriesName)
                .last("LIMIT 1");
        if (brandId != null) {
            wrapper.eq("brand_id", brandId);
        }
        Series series = seriesService.getOne(wrapper);
        if (series == null) {
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "row " + item.getRowNo() + ": series not found by name");
        }
        return series.getId();
    }

    private Long resolveCategoryId(GoodsBatchUpsertItemDTO item) {
        return resolveCategoryId(item, true);
    }

    private Long resolveCategoryId(GoodsBatchUpsertItemDTO item, boolean failWhenMissingName) {
        if (item.getCategoryId() != null) {
            Category category = categoryService.getByIdNotDeleted(item.getCategoryId());
            if (category == null) {
                throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "row " + item.getRowNo() + ": category not found");
            }
            return category.getId();
        }
        String categoryName = trimToNull(item.getCategoryName());
        if (!StringUtils.hasText(categoryName)) {
            return failWhenMissingName ? null : null;
        }
        Category category = categoryService.getOne(new QueryWrapper<Category>()
                .eq("name", categoryName)
                .last("LIMIT 1"));
        if (category == null) {
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "row " + item.getRowNo() + ": category not found by name");
        }
        return category.getId();
    }

    private Long resolveMakerId(GoodsBatchUpsertItemDTO item) {
        return resolveMakerId(item, true);
    }

    private Long resolveMakerId(GoodsBatchUpsertItemDTO item, boolean failWhenMissingName) {
        if (item.getMakerId() != null) {
            Maker maker = makerService.getByIdNotDeleted(item.getMakerId());
            if (maker == null) {
                throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "row " + item.getRowNo() + ": maker not found");
            }
            return maker.getId();
        }
        String makerName = trimToNull(item.getMakerName());
        if (!StringUtils.hasText(makerName)) {
            return failWhenMissingName ? null : null;
        }
        Maker maker = makerService.getOne(new QueryWrapper<Maker>()
                .eq("name", makerName)
                .last("LIMIT 1"));
        if (maker == null) {
            throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "row " + item.getRowNo() + ": maker not found by name");
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
        throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                "unsupported status value: " + value);
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
        throw new BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                "unsupported flag value: " + value);
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
        return trimToNull(current.getMessage()) == null ? "unknown error" : current.getMessage();
    }

    private record ExistingGoodsTarget(Goods goods, GoodsSku sku, GoodsImage image) {
    }

    private void validatePriceUpdateFields(java.math.BigDecimal updatePrice, LocalDateTime priceUpdateTime) {
        if (updatePrice != null && priceUpdateTime == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
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
