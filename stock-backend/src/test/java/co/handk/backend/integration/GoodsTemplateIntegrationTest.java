package co.handk.backend.integration;

import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.entity.Brand;
import co.handk.backend.entity.Category;
import co.handk.backend.entity.Goods;
import co.handk.backend.entity.Maker;
import co.handk.backend.entity.Series;
import co.handk.backend.service.BrandService;
import co.handk.backend.service.CategoryService;
import co.handk.backend.service.GoodsService;
import co.handk.backend.service.MakerService;
import co.handk.backend.service.SeriesService;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.dto.query.GoodsQueryDTO;
import co.handk.common.model.vo.GoodsBatchUpsertResultVO;
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
    private static final int TEMPLATE_COL_GOODS_NAME = 2;
    private static final int TEMPLATE_COL_BRAND_NAME = 4;
    private static final int TEMPLATE_COL_SERIES_NAME = 5;
    private static final int TEMPLATE_COL_CATEGORY_NAME = 6;
    private static final int TEMPLATE_COL_MAKER_NAME = 7;
    private static final int TEMPLATE_COL_SKU_CODE = 11;
    private static final int TEMPLATE_COL_SKU_NAME = 12;
    private static final int TEMPLATE_COL_CURRENCY = 14;
    private static final int TEMPLATE_COL_SKU_STATUS = 21;
    private static final int TEMPLATE_COL_GOODS_STATUS = 25;

    @Autowired
    private GoodsService goodsService;
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
        assertTrue(result.getRows().get(0).getMessage().contains("outside selected filter scope"));
        assertTrue(result.getErrorReportFileName().endsWith(".xlsx"));
        assertNotNull(result.getErrorReportBase64());
        assertFalse(result.getErrorReportBase64().isBlank());

        try (XSSFWorkbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(Base64.getDecoder().decode(result.getErrorReportBase64())))) {
            Row headerRow = workbook.getSheetAt(0).getRow(0);
            Map<String, Integer> headerIndexes = resolveHeaderIndexes(headerRow);
            Integer actionColumn = headerIndexes.get("Import Action");
            Integer messageColumn = headerIndexes.get("Import Message");
            assertNotNull(actionColumn);
            assertNotNull(messageColumn);

            Row failedRow = workbook.getSheetAt(0).getRow(2);
            assertNotNull(failedRow);
            assertEquals("FAILED", failedRow.getCell(actionColumn).getStringCellValue());
            assertTrue(failedRow.getCell(messageColumn).getStringCellValue().contains("outside selected filter scope"));
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
            row.createCell(TEMPLATE_COL_SKU_STATUS).setCellValue("1");
            row.createCell(TEMPLATE_COL_GOODS_STATUS).setCellValue("1");
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
}
