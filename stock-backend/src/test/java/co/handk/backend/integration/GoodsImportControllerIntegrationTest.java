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
import co.handk.backend.service.PermissionQueryService;
import co.handk.backend.service.SeriesService;
import co.handk.backend.util.StringRedisUtil;
import co.handk.common.constant.RedisKey;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class GoodsImportControllerIntegrationTest {

    private static final String API_CONTEXT_PATH = "/api";
    private static final Long ADMIN_USER_ID = 1L;
    private static final String ADMIN_TOKEN = "test-admin-token";

    private static final int TEMPLATE_COL_GOODS_NAME = 1;
    private static final int TEMPLATE_COL_ENGLISH_NAME = 2;
    private static final int TEMPLATE_COL_BRAND_NAME = 3;
    private static final int TEMPLATE_COL_BRAND_ENGLISH_NAME = 4;
    private static final int TEMPLATE_COL_SERIES_NAME = 5;
    private static final int TEMPLATE_COL_SERIES_ENGLISH_NAME = 6;
    private static final int TEMPLATE_COL_CATEGORY_NAME = 7;
    private static final int TEMPLATE_COL_MAKER_NAME = 8;
    private static final int TEMPLATE_COL_MAKER_ENGLISH_NAME = 9;
    private static final int TEMPLATE_COL_SKU_CODE = 10;
    private static final int TEMPLATE_COL_SKU_NAME = 11;
    private static final int TEMPLATE_COL_PRICE = 12;
    private static final int TEMPLATE_COL_CURRENCY = 13;

    @Autowired
    private MockMvc mockMvc;
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
    @Autowired
    private PermissionQueryService permissionQueryService;

    @MockBean
    private StringRedisUtil stringRedisUtil;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(ADMIN_USER_ID);
        when(stringRedisUtil.setIfAbsent(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.any(TimeUnit.class)))
                .thenReturn(true);
        when(stringRedisUtil.getExpire(ArgumentMatchers.anyString(), ArgumentMatchers.any(TimeUnit.class)))
                .thenReturn(null);
        when(stringRedisUtil.expire(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
                .thenReturn(true);
        when(stringRedisUtil.delete(ArgumentMatchers.anyString())).thenReturn(true);
        when(stringRedisUtil.get(ArgumentMatchers.anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            if ((RedisKey.LOGIN_TOKEN + ADMIN_TOKEN).equals(key)) {
                return String.valueOf(ADMIN_USER_ID);
            }
            if ((RedisKey.LOGIN_USER + ADMIN_USER_ID).equals(key)) {
                return ADMIN_TOKEN;
            }
            return null;
        });
        assertTrue(permissionQueryService.isSuperAdmin(ADMIN_USER_ID));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void importUpsertEndpointCreatesGoodsAndMasterDataFromTemplate() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String goodsName = "api-import-goods-" + suffix;
        String brandName = "api-import-brand-" + suffix;
        String brandEnglishName = "api import brand en " + suffix;
        String seriesName = "api-import-series-" + suffix;
        String seriesEnglishName = "api import series en " + suffix;
        String categoryName = "api-import-category-" + suffix;
        String makerName = "api-import-maker-" + suffix;
        String makerEnglishName = "api import maker en " + suffix;
        String skuCode = "api-import-sku-" + suffix;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "goods-import.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                buildImportWorkbook(goodsName, brandName, brandEnglishName, seriesName,
                        seriesEnglishName, categoryName, makerName, makerEnglishName, skuCode)
        );

        mockMvc.perform(multipart(API_CONTEXT_PATH + "/goods/import/upsert")
                        .file(file)
                        .contextPath(API_CONTEXT_PATH)
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.createdCount").value(1))
                .andExpect(jsonPath("$.data.failureCount").value(0))
                .andExpect(jsonPath("$.data.errorReportBase64").doesNotExist());

        Brand brand = findBrand(brandName);
        Series series = findSeries(seriesName);
        Category category = findCategory(categoryName);
        Maker maker = findMaker(makerName);
        Goods goods = findGoods(goodsName);
        GoodsSku sku = findSku(skuCode);

        assertNotNull(brand);
        assertNotNull(series);
        assertNotNull(category);
        assertNotNull(maker);
        assertNotNull(goods);
        assertNotNull(sku);
        assertEquals(brandEnglishName, brand.getEnglishName());
        assertEquals(seriesEnglishName, series.getEnglishName());
        assertEquals(makerEnglishName, maker.getEnglishName());
        assertEquals(brand.getId(), series.getBrandId());
        assertEquals(series.getId(), maker.getSeriesId());
        assertEquals(brand.getId(), goods.getBrandId());
        assertEquals(series.getId(), goods.getSeriesId());
        assertEquals(category.getId(), goods.getCategoryId());
        assertEquals(maker.getId(), goods.getMakerId());
        assertEquals(goods.getId(), sku.getGoodsId());
        assertEquals(new BigDecimal("1200.00"), sku.getPrice());
    }

    private byte[] buildImportWorkbook(String goodsName,
                                       String brandName,
                                       String brandEnglishName,
                                       String seriesName,
                                       String seriesEnglishName,
                                       String categoryName,
                                       String makerName,
                                       String makerEnglishName,
                                       String skuCode) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        goodsService.downloadBatchTemplate(null, response);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()));
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Row row = workbook.getSheetAt(0).createRow(2);
            row.createCell(TEMPLATE_COL_GOODS_NAME).setCellValue(goodsName);
            row.createCell(TEMPLATE_COL_ENGLISH_NAME).setCellValue(goodsName);
            row.createCell(TEMPLATE_COL_BRAND_NAME).setCellValue(brandName);
            row.createCell(TEMPLATE_COL_BRAND_ENGLISH_NAME).setCellValue(brandEnglishName);
            row.createCell(TEMPLATE_COL_SERIES_NAME).setCellValue(seriesName);
            row.createCell(TEMPLATE_COL_SERIES_ENGLISH_NAME).setCellValue(seriesEnglishName);
            row.createCell(TEMPLATE_COL_CATEGORY_NAME).setCellValue(categoryName);
            row.createCell(TEMPLATE_COL_MAKER_NAME).setCellValue(makerName);
            row.createCell(TEMPLATE_COL_MAKER_ENGLISH_NAME).setCellValue(makerEnglishName);
            row.createCell(TEMPLATE_COL_SKU_CODE).setCellValue(skuCode);
            row.createCell(TEMPLATE_COL_SKU_NAME).setCellValue(goodsName);
            row.createCell(TEMPLATE_COL_PRICE).setCellValue("1200.00");
            row.createCell(TEMPLATE_COL_CURRENCY).setCellValue("JPY");
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private Brand findBrand(String name) {
        return brandService.getOne(new QueryWrapper<Brand>().eq("name", name).last("LIMIT 1"));
    }

    private Series findSeries(String name) {
        return seriesService.getOne(new QueryWrapper<Series>().eq("name", name).last("LIMIT 1"));
    }

    private Category findCategory(String name) {
        return categoryService.getOne(new QueryWrapper<Category>().eq("name", name).last("LIMIT 1"));
    }

    private Maker findMaker(String name) {
        return makerService.getOne(new QueryWrapper<Maker>().eq("name", name).last("LIMIT 1"));
    }

    private Goods findGoods(String name) {
        return goodsService.getOne(new QueryWrapper<Goods>().eq("name", name).last("LIMIT 1"));
    }

    private GoodsSku findSku(String skuCode) {
        return goodsSkuService.getOne(new QueryWrapper<GoodsSku>().eq("sku_code", skuCode).last("LIMIT 1"));
    }
}
