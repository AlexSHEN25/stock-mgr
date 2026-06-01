package co.handk.client;

import co.handk.client.util.ModuleMeta;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebPageAlignmentTest {

    @Test
    void stockMenuMatchesWebPageOrder() throws Exception {
        String mainFxml = loadText("fxml/main.fxml");

        assertInOrder(mainFxml, List.of(
                "userData=\"stock\"",
                "userData=\"stockOrder\"",
                "userData=\"stockOrderItem\"",
                "userData=\"stockType\"",
                "userData=\"stockRecord\"",
                "userData=\"priceRecord\""
        ));
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
    void goodsTableMetadataMatchesWebPageBehavior() {
        assertEquals("\u306f\u3044", ModuleMeta.optionByValue("goods", "isHot", "1").label);
        assertEquals("\u3044\u3044\u3048", ModuleMeta.optionByValue("goods", "isHot", "0").label);
        assertEquals(
                List.of("skuId", "goodsName", "skuCode", "statusDesc", "createTime", "updateTime"),
                ModuleMeta.orderedColumns("goods", List.of(
                        "id", "skuId", "goodsName", "brandId", "skuCode", "costPrice",
                        "status", "statusDesc", "createTime", "updateTime")));
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
