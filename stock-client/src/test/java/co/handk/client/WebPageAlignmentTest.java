package co.handk.client;

import co.handk.client.util.ModuleMeta;
import co.handk.client.constant.ModuleEndpointStrategy;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebPageAlignmentTest {

    @Test
    void stockMenuMatchesWebPageOrder() throws Exception {
        String mainFxml = loadText("fxml/main.fxml");

        assertInOrder(mainFxml, List.of(
                "userData=\"selfStock\"",
                "userData=\"handleStock\"",
                "userData=\"stockOrder\"",
                "userData=\"stockOrderItem\"",
                "userData=\"stockType\"",
                "userData=\"stockRecord\"",
                "userData=\"priceRecord\""
        ));
    }

    @Test
    void splitStockMenusUseScopedBackendEndpoints() {
        assertEquals("/stock/self/page", ModuleEndpointStrategy.pagePath("selfStock"));
        assertEquals("/stock/inbound", ModuleEndpointStrategy.createPath("selfStock"));
        assertEquals("/stock/self/1", ModuleEndpointStrategy.detailPath("selfStock", "1"));
        assertEquals("/stock/self", ModuleEndpointStrategy.updatePath("selfStock", "1"));
        assertEquals("/stock/self/1", ModuleEndpointStrategy.deletePath("selfStock", "1"));
        assertEquals("/stock/self/batch", ModuleEndpointStrategy.batchDeletePath("selfStock"));
        assertEquals("/stock/handle/page", ModuleEndpointStrategy.pagePath("handleStock"));
        assertEquals("/stock/inbound", ModuleEndpointStrategy.createPath("handleStock"));
        assertEquals("/stock/handle/1", ModuleEndpointStrategy.detailPath("handleStock", "1"));
        assertEquals("/stock/handle", ModuleEndpointStrategy.updatePath("handleStock", "1"));
        assertEquals("/stock/handle/1", ModuleEndpointStrategy.deletePath("handleStock", "1"));
        assertEquals("/stock/handle/batch", ModuleEndpointStrategy.batchDeletePath("handleStock"));
    }

    @Test
    void headerOffersPasswordChangeBeforeLogoutLikeWebPage() throws Exception {
        String mainFxml = loadText("fxml/main.fxml");

        assertInOrder(mainFxml, List.of(
                "onAction=\"#onChangePassword\"",
                "onAction=\"#onLogout\""
        ));
    }

    @Test
    void seriesAndStockRecordMetadataMatchWebPagePresets() {
        assertEquals(
                List.of("id", "name", "englishName", "brandId", "content", "status"),
                ModuleMeta.queryFields("series"));
        assertTrue(ModuleMeta.formFields("stockRecord").containsAll(List.of(
                "bizNo", "orderId", "orderItemId", "stockId", "goodsId", "skuId",
                "warehouseId", "changeQty", "sourceType", "orderType", "bizDate", "price")));
        assertTrue(ModuleMeta.isRequiredFormField("stockRecord", "bizNo"));
        assertTrue(ModuleMeta.isRequiredFormField("stockRecord", "sourceType"));
        assertFalse(ModuleMeta.isRequiredFormField("customer", "customerCode"));
        assertFalse(ModuleMeta.isRequiredFormField("customer", "name"));
        assertFalse(ModuleMeta.isRequiredFormField("customer", "status"));
    }

    @Test
    void stockOrderSourceIdIsBackendMaintained() {
        assertTrue(ModuleMeta.queryFields("stockOrder").contains("sourceId"));
        assertFalse(ModuleMeta.formFields("stockOrder").contains("sourceId"));
    }

    @Test
    void splitStockFormsDefaultWarehouseAndFilterGoodsByWarehouse() {
        assertEquals(
                List.of("warehouseId", "goodsId", "skuId", "sourceType", "stockTypeId", "quantity", "remark"),
                ModuleMeta.formFields("selfStock"));
        assertEquals(ModuleMeta.formFields("selfStock"), ModuleMeta.formFields("handleStock"));
        assertEquals(Map.of("name", "\u81ea\u793e\u5728\u5eab"),
                ModuleMeta.initialRelationFilters("selfStock", "warehouseId"));
        assertEquals(Map.of("name", "\u30cf\u30f3\u30c9\u30eb\u5728\u5eab"),
                ModuleMeta.initialRelationFilters("handleStock", "warehouseId"));
        assertTrue(ModuleMeta.shouldAutoSelectFirstRelation("selfStock", "warehouseId"));
        assertTrue(ModuleMeta.shouldAutoSelectFirstRelation("handleStock", "warehouseId"));
    }

    @Test
    void goodsTableMetadataMatchesWebPageBehavior() {
        assertEquals("\u306f\u3044", ModuleMeta.optionByValue("goods", "isHot", "1").label);
        assertEquals("\u3044\u3044\u3048", ModuleMeta.optionByValue("goods", "isHot", "0").label);
        assertEquals(
                List.of("skuId", "goodsName", "skuCode", "statusDesc", "createTime", "updateTime"),
                ModuleMeta.orderedColumns("goods", List.of(
                        "id", "skuId", "goodsName", "brandId", "skuCode", "costPrice",
                        "status", "statusDesc", "createTime", "updateTime")));
    }

    @Test
    void modulesWithoutPresetBuildFormFieldsFromTableColumnsLikeWebPage() {
        assertTrue(ModuleMeta.formFields("permission").isEmpty());
        assertEquals(
                List.of("name", "code", "parentId", "status"),
                ModuleMeta.resolvedFormFields(
                        "permission",
                        List.of("__selected", "id", "name", "code", "parentName", "statusDesc", "status", "createTime")));
    }

    @Test
    void modulesWithoutRowsFallBackToQueryFieldsForCreateDialog() {
        assertTrue(ModuleMeta.formFields("customerLevel").isEmpty());
        assertEquals(
                List.of("name", "discount", "remark", "status"),
                ModuleMeta.resolvedFormFields("customerLevel", List.of()));
    }

    private static void assertInOrder(String source, List<String> values) {
        int last = -1;
        for (String value : values) {
            int current = source.indexOf(value);
            assertTrue(current > last, "Expected ordered value in FXML: " + value);
            last = current;
        }
    }

    private static String loadText(String path) throws Exception {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            Objects.requireNonNull(in, "Resource not found: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
