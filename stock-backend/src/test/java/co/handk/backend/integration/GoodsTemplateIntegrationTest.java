package co.handk.backend.integration;

import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.entity.Brand;
import co.handk.backend.entity.Category;
import co.handk.backend.entity.Goods;
import co.handk.backend.entity.GoodsSku;
import co.handk.backend.entity.Maker;
import co.handk.backend.entity.Series;
import co.handk.backend.service.BrandService;
import co.handk.backend.service.CategoryService;
import co.handk.backend.service.GoodsService;
import co.handk.backend.service.GoodsSkuService;
import co.handk.backend.service.MakerService;
import co.handk.backend.service.SeriesService;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.dto.query.GoodsQueryDTO;
import co.handk.common.model.vo.GoodsBatchUpsertResultVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
@Transactional
class GoodsTemplateIntegrationTest {

    private static final Long ADMIN_USER_ID = 1L;
    private static final int TEMPLATE_COL_GOODS_NAME = 1;
    private static final int TEMPLATE_COL_BRAND_NAME = 3;
    private static final int TEMPLATE_COL_SERIES_NAME = 5;
    private static final int TEMPLATE_COL_CATEGORY_NAME = 7;
    private static final int TEMPLATE_COL_MAKER_NAME = 8;
    private static final int TEMPLATE_COL_SKU_CODE = 10;
    private static final int TEMPLATE_COL_SKU_NAME = 11;
    private static final int TEMPLATE_COL_PRICE = 12;
    private static final int TEMPLATE_COL_CURRENCY = 13;

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private GoodsSkuService goodsSkuService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SeriesService seriesService;
    @Autowired
    private MakerService makerService;
    @Autowired
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(ADMIN_USER_ID);
    }

    @Test
    void templateRespectsBrandFilterScope() throws Exception {
        Brand brandA = saveBrand("template-brand-a");
        Brand brandB = saveBrand("template-brand-b");
        Series seriesA = saveSeries("template-series-a", brandA.getId());
        Series seriesB = saveSeries("template-series-b", brandB.getId());
        Maker makerA = saveMaker("template-maker-a", seriesA.getId());
        saveMaker("template-maker-b", seriesB.getId());
        Category category = saveCategory("template-category");

        saveGoods("template-goods-a", brandA.getId(), seriesA.getId(), category.getId(), makerA.getId());
        saveGoods("template-goods-b", brandB.getId(), seriesB.getId(), category.getId(), null);

        GoodsQueryDTO query = new GoodsQueryDTO();
        query.setBrandId(brandA.getId());

        MockHttpServletResponse response = new MockHttpServletResponse();
        goodsService.downloadBatchTemplate(query, response);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            var dictionarySheet = workbook.getSheetAt(2);
            List<String> dictionaryValues = collectSheetValues(dictionarySheet);
            assertTrue(dictionaryValues.contains(String.valueOf(brandA.getId())));
            assertTrue(dictionaryValues.contains(brandA.getName()));
            assertTrue(dictionaryValues.contains(String.valueOf(seriesA.getId())));
            assertTrue(dictionaryValues.contains(seriesA.getName()));
            assertTrue(dictionaryValues.contains(String.valueOf(makerA.getId())));
            assertTrue(dictionaryValues.contains(makerA.getName()));
            assertFalse(dictionaryValues.contains(brandB.getName()));
            assertFalse(dictionaryValues.contains(seriesB.getName()));

            var validationSheet = workbook.getSheetAt(3);
            boolean hasScopedSeriesRange = false;
            for (int rowIndex = 0; rowIndex <= validationSheet.getLastRowNum(); rowIndex++) {
                var row = validationSheet.getRow(rowIndex);
                if (row == null || row.getCell(0) == null) {
                    continue;
                }
                if (("SERIES_NAME_" + brandA.getId()).equals(row.getCell(0).getStringCellValue())) {
                    hasScopedSeriesRange = true;
                    assertEquals(seriesA.getName(), validationSheet.getRow(rowIndex + 1).getCell(0).getStringCellValue());
                }
            }
            assertTrue(hasScopedSeriesRange);
        }
    }

    @Test
    void templateOnlyContainsNecessaryImportColumns() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        goodsService.downloadBatchTemplate(null, response);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            Row headerRow = workbook.getSheetAt(0).getRow(0);
            Row noteRow = workbook.getSheetAt(0).getRow(1);
            assertNotNull(headerRow);
            assertNotNull(noteRow);
            assertTrue(response.getHeader("Content-Disposition").contains("goods-import-template.xlsx"));
            assertEquals(15, headerRow.getLastCellNum());
            assertEquals(15, noteRow.getLastCellNum());
            assertTrue(collectRowValues(headerRow).containsAll(List.of(
                    "id", "名称", "英文名称", "ブランド", "シリーズ", "カテゴリ",
                    "メーカー", "品番", "品名", "価格", "通貨", "説明"
            )));
            assertTrue(collectRowValues(headerRow).contains("ブランド英語名"));
            assertTrue(collectRowValues(headerRow).contains("シリーズ英語名"));
            assertTrue(collectRowValues(headerRow).contains("メーカー英語名"));
        }
    }

    @Test
    void exportOnlyContainsNecessaryGoodsColumns() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        goodsService.exportGoods(new GoodsQueryDTO(), response);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            Row headerRow = workbook.getSheetAt(0).getRow(0);
            assertNotNull(headerRow);
            assertEquals(15, headerRow.getLastCellNum());
            assertTrue(collectRowValues(headerRow).containsAll(List.of(
                    "id", "名称", "英文名称", "ブランド", "シリーズ", "カテゴリ",
                    "メーカー", "品番", "品名", "価格", "通貨", "説明"
            )));
            assertTrue(collectRowValues(headerRow).contains("ブランド英語名"));
            assertTrue(collectRowValues(headerRow).contains("シリーズ英語名"));
            assertTrue(collectRowValues(headerRow).contains("メーカー英語名"));
        }
    }

    @Test
    void exportUsesSameWorkbookLayoutAsTemplate() throws Exception {
        MockHttpServletResponse templateResponse = new MockHttpServletResponse();
        goodsService.downloadBatchTemplate(null, templateResponse);
        MockHttpServletResponse exportResponse = new MockHttpServletResponse();
        goodsService.exportGoods(new GoodsQueryDTO(), exportResponse);

        try (XSSFWorkbook templateWorkbook = new XSSFWorkbook(new ByteArrayInputStream(templateResponse.getContentAsByteArray()));
             XSSFWorkbook exportWorkbook = new XSSFWorkbook(new ByteArrayInputStream(exportResponse.getContentAsByteArray()))) {
            assertEquals(templateWorkbook.getNumberOfSheets(), exportWorkbook.getNumberOfSheets());
            for (int sheetIndex = 0; sheetIndex < templateWorkbook.getNumberOfSheets(); sheetIndex++) {
                assertEquals(templateWorkbook.getSheetName(sheetIndex), exportWorkbook.getSheetName(sheetIndex));
                assertEquals(templateWorkbook.isSheetHidden(sheetIndex), exportWorkbook.isSheetHidden(sheetIndex));
            }

            Row templateHeader = templateWorkbook.getSheetAt(0).getRow(0);
            Row exportHeader = exportWorkbook.getSheetAt(0).getRow(0);
            Row templateNote = templateWorkbook.getSheetAt(0).getRow(1);
            Row exportNote = exportWorkbook.getSheetAt(0).getRow(1);
            assertNotNull(templateHeader);
            assertNotNull(exportHeader);
            assertNotNull(templateNote);
            assertNotNull(exportNote);
            assertEquals(collectRowValues(templateHeader), collectRowValues(exportHeader));
            assertEquals(collectRowValues(templateNote), collectRowValues(exportNote));
            assertEquals(templateWorkbook.getSheetAt(0).getDataValidations().size(),
                    exportWorkbook.getSheetAt(0).getDataValidations().size());
        }
    }

    @Test
    void templateIsBlankDataVersionOfGoodsExport() throws Exception {
        Brand brand = saveBrand("blank-template-brand");
        Category category = saveCategory("blank-template-category");
        Goods goods = saveGoods("blank-template-goods", brand.getId(), null, category.getId(), null);
        saveSku(goods.getId(), "blank-template-sku");

        MockHttpServletResponse templateResponse = new MockHttpServletResponse();
        goodsService.downloadBatchTemplate(null, templateResponse);
        MockHttpServletResponse exportResponse = new MockHttpServletResponse();
        goodsService.exportGoods(new GoodsQueryDTO(), exportResponse);

        try (XSSFWorkbook templateWorkbook = new XSSFWorkbook(new ByteArrayInputStream(templateResponse.getContentAsByteArray()));
             XSSFWorkbook exportWorkbook = new XSSFWorkbook(new ByteArrayInputStream(exportResponse.getContentAsByteArray()))) {
            assertEquals(1, templateWorkbook.getSheetAt(0).getLastRowNum());
            assertTrue(exportWorkbook.getSheetAt(0).getLastRowNum() >= 2);
            assertEquals(collectRowValues(exportWorkbook.getSheetAt(0).getRow(0)),
                    collectRowValues(templateWorkbook.getSheetAt(0).getRow(0)));
            assertEquals(collectRowValues(exportWorkbook.getSheetAt(0).getRow(1)),
                    collectRowValues(templateWorkbook.getSheetAt(0).getRow(1)));
        }
    }

    @Test
    void importUpdatePreservesFieldsNotIncludedInTemplate() throws Exception {
        Brand brand = saveBrand("preserve-brand");
        Category category = saveCategory("preserve-category");
        Goods goods = saveGoods("preserve-goods", brand.getId(), null, category.getId(), null);
        goods.setIsHot(1);
        goods.setSort(9);
        goods.setStatus(StatusEnum.FOBBIDEN.getCode());
        assertTrue(goodsService.updateById(goods));
        GoodsSku sku = saveSku(goods.getId(), "preserve-sku");
        sku.setCostPrice(new BigDecimal("321.00"));
        sku.setBarcode("KEEP-BARCODE");
        sku.setWeight(new BigDecimal("12.30"));
        sku.setVolume(new BigDecimal("45.60"));
        sku.setStatus(StatusEnum.FOBBIDEN.getCode());
        assertTrue(goodsSkuService.updateById(sku));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "goods-preserve.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                buildPreserveUpdateWorkbook(goods, brand, category, sku)
        );

        GoodsBatchUpsertResultVO result = goodsService.importGoods(file, null);

        assertEquals(1, result.getSuccessCount());
        Goods updatedGoods = goodsService.getById(goods.getId());
        GoodsSku updatedSku = goodsSkuService.getOne(new QueryWrapper<GoodsSku>()
                .eq("id", sku.getId())
                .last("LIMIT 1"));
        assertEquals(1, updatedGoods.getIsHot());
        assertEquals(9, updatedGoods.getSort());
        assertEquals(StatusEnum.FOBBIDEN.getCode(), updatedGoods.getStatus());
        assertEquals(new BigDecimal("321.00"), updatedSku.getCostPrice());
        assertEquals("KEEP-BARCODE", updatedSku.getBarcode());
        assertEquals(new BigDecimal("12.30"), updatedSku.getWeight());
        assertEquals(new BigDecimal("45.60"), updatedSku.getVolume());
        assertEquals(StatusEnum.FOBBIDEN.getCode(), updatedSku.getStatus());
    }

    @Test
    void importScopeFailureReturnsHighlightedErrorWorkbook() throws Exception {
        Brand brandA = saveBrand("scope-brand-a");
        Brand brandB = saveBrand("scope-brand-b");
        Series seriesA = saveSeries("scope-series-a", brandA.getId());
        Series seriesB = saveSeries("scope-series-b", brandB.getId());
        Maker makerA = saveMaker("scope-maker-a", seriesA.getId());
        Maker makerB = saveMaker("scope-maker-b", seriesB.getId());
        Category category = saveCategory("scope-category");

        saveGoods("scope-goods-a", brandA.getId(), seriesA.getId(), category.getId(), makerA.getId());
        saveGoods("scope-goods-b", brandB.getId(), seriesB.getId(), category.getId(), makerB.getId());

        GoodsQueryDTO query = new GoodsQueryDTO();
        query.setBrandId(brandA.getId());

        byte[] importBytes = buildScopeMismatchImportWorkbook(query, brandB, seriesB, category, makerB);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "goods-scope-mismatch.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                importBytes
        );

        GoodsBatchUpsertResultVO result = goodsService.importGoods(file, query);

        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals(1, result.getRows().size());
        assertFalse(result.getRows().get(0).getSuccess());
        assertTrue(result.getRows().get(0).getMessage().contains("選択された絞り込み条件の範囲外"));
        assertTrue(result.getErrorReportFileName().endsWith(".xlsx"));
        assertNotNull(result.getErrorReportBase64());
        assertFalse(result.getErrorReportBase64().isBlank());

        try (XSSFWorkbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(Base64.getDecoder().decode(result.getErrorReportBase64())))) {
            Row headerRow = workbook.getSheetAt(0).getRow(0);
            Map<String, Integer> headerIndexes = resolveHeaderIndexes(headerRow);
            Integer actionColumn = headerIndexes.get("取込結果");
            Integer messageColumn = headerIndexes.get("メッセージ");
            assertNotNull(actionColumn);
            assertNotNull(messageColumn);

            Row failedRow = workbook.getSheetAt(0).getRow(2);
            assertNotNull(failedRow);
            assertEquals("失敗", failedRow.getCell(actionColumn).getStringCellValue());
            assertTrue(failedRow.getCell(messageColumn).getStringCellValue().contains("選択された絞り込み条件の範囲外"));
            assertEquals(IndexedColors.ROSE.getIndex(), failedRow.getCell(0).getCellStyle().getFillForegroundColor());
            assertEquals(IndexedColors.ROSE.getIndex(),
                    failedRow.getCell(messageColumn).getCellStyle().getFillForegroundColor());
        }
    }

    private Brand saveBrand(String name) {
        Brand brand = new Brand();
        brand.setName(name);
        brand.setStatus(StatusEnum.NOMAL.getCode());
        assertTrue(brandService.save(brand));
        return brand;
    }

    private Series saveSeries(String name, Long brandId) {
        Series series = new Series();
        series.setName(name);
        series.setBrandId(brandId);
        series.setStatus(StatusEnum.NOMAL.getCode());
        assertTrue(seriesService.save(series));
        return series;
    }

    private Maker saveMaker(String name, Long seriesId) {
        Maker maker = new Maker();
        maker.setName(name);
        maker.setSeriesId(seriesId);
        maker.setStatus(StatusEnum.NOMAL.getCode());
        assertTrue(makerService.save(maker));
        return maker;
    }

    private Category saveCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setStatus(StatusEnum.NOMAL.getCode());
        assertTrue(categoryService.save(category));
        return category;
    }

    private Goods saveGoods(String name, Long brandId, Long seriesId, Long categoryId, Long makerId) {
        Goods goods = new Goods();
        goods.setName(name);
        goods.setBrandId(brandId);
        goods.setSeriesId(seriesId);
        goods.setCategoryId(categoryId);
        goods.setMakerId(makerId);
        goods.setStatus(StatusEnum.NOMAL.getCode());
        assertTrue(goodsService.save(goods));
        return goods;
    }

    private GoodsSku saveSku(Long goodsId, String skuCode) {
        GoodsSku sku = new GoodsSku();
        sku.setGoodsId(goodsId);
        sku.setSkuCode(skuCode);
        sku.setSkuName(skuCode);
        sku.setPrice(new BigDecimal("100.00"));
        sku.setCurrency("JPY");
        sku.setStatus(StatusEnum.NOMAL.getCode());
        assertTrue(goodsSkuService.save(sku));
        return sku;
    }

    private List<String> collectSheetValues(org.apache.poi.ss.usermodel.Sheet sheet) {
        List<String> values = new ArrayList<>();
        for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            var row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            for (int cellIndex = row.getFirstCellNum(); cellIndex < row.getLastCellNum(); cellIndex++) {
                if (cellIndex < 0) {
                    continue;
                }
                var cell = row.getCell(cellIndex);
                if (cell != null) {
                    values.add(cell.toString());
                }
            }
        }
        return values;
    }

    private byte[] buildScopeMismatchImportWorkbook(GoodsQueryDTO query,
                                                    Brand brand,
                                                    Series series,
                                                    Category category,
                                                    Maker maker) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        goodsService.downloadBatchTemplate(query, response);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()));
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Row row = workbook.getSheetAt(0).createRow(2);
            row.createCell(TEMPLATE_COL_GOODS_NAME).setCellValue("scope-import-goods");
            row.createCell(TEMPLATE_COL_BRAND_NAME).setCellValue(brand.getName());
            row.createCell(TEMPLATE_COL_SERIES_NAME).setCellValue(series.getName());
            row.createCell(TEMPLATE_COL_CATEGORY_NAME).setCellValue(category.getName());
            row.createCell(TEMPLATE_COL_MAKER_NAME).setCellValue(maker.getName());
            row.createCell(TEMPLATE_COL_SKU_CODE).setCellValue("scope-mismatch-sku");
            row.createCell(TEMPLATE_COL_SKU_NAME).setCellValue("scope-import-sku");
            row.createCell(TEMPLATE_COL_CURRENCY).setCellValue("JPY");
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] buildPreserveUpdateWorkbook(Goods goods, Brand brand, Category category, GoodsSku sku) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        goodsService.downloadBatchTemplate(null, response);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()));
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Row row = workbook.getSheetAt(0).createRow(2);
            row.createCell(0).setCellValue(goods.getId());
            row.createCell(TEMPLATE_COL_GOODS_NAME).setCellValue("preserve-goods-updated");
            row.createCell(TEMPLATE_COL_BRAND_NAME).setCellValue(brand.getName());
            row.createCell(TEMPLATE_COL_CATEGORY_NAME).setCellValue(category.getName());
            row.createCell(TEMPLATE_COL_SKU_CODE).setCellValue(sku.getSkuCode());
            row.createCell(TEMPLATE_COL_SKU_NAME).setCellValue("preserve-sku-updated");
            row.createCell(TEMPLATE_COL_PRICE).setCellValue("200.00");
            row.createCell(TEMPLATE_COL_CURRENCY).setCellValue("JPY");
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private Map<String, Integer> resolveHeaderIndexes(Row headerRow) {
        Map<String, Integer> indexes = new HashMap<>();
        for (int cellIndex = headerRow.getFirstCellNum(); cellIndex < headerRow.getLastCellNum(); cellIndex++) {
            if (cellIndex < 0 || headerRow.getCell(cellIndex) == null) {
                continue;
            }
            indexes.put(headerRow.getCell(cellIndex).getStringCellValue(), cellIndex);
        }
        return indexes;
    }

    private List<String> collectRowValues(Row row) {
        List<String> values = new ArrayList<>();
        for (int cellIndex = row.getFirstCellNum(); cellIndex < row.getLastCellNum(); cellIndex++) {
            values.add(row.getCell(cellIndex).getStringCellValue());
        }
        return values;
    }
}
