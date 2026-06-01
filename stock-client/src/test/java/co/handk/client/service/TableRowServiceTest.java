package co.handk.client.service;

import javafx.beans.property.SimpleBooleanProperty;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableRowServiceTest {

    private final TableRowService service = new TableRowService();

    @Test
    void normalizeForUpdateDropsDisplayFieldsAndConvertsNumbers() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("__selected", true);
        row.put("id", " 12 ");
        row.put("goodsName", "display only");
        row.put("englishName", " English name ");
        row.put("statusDesc", "display status");
        row.put("beforeQty", 2);
        row.put("afterQty", 5);
        row.put("createTime", "2026-06-01T10:00:00");
        row.put("price", " 19.5 ");
        row.put("remark", " keep ");

        JSONObject dto = service.normalizeForUpdate("goods", row, "__selected");

        assertEquals(12L, dto.getLong("id"));
        assertEquals(19.5, dto.getDouble("price"));
        assertEquals("keep", dto.getString("remark"));
        assertEquals("English name", dto.getString("englishName"));
        assertFalse(dto.has("__selected"));
        assertFalse(dto.has("goodsName"));
        assertFalse(dto.has("statusDesc"));
        assertFalse(dto.has("beforeQty"));
        assertFalse(dto.has("afterQty"));
        assertFalse(dto.has("createTime"));
    }

    @Test
    void formatCellValueFormatsSupportedDatesAndKeepsUnknownValues() {
        assertEquals("2026-06-01 10:20:30", service.formatCellValue("createTime", "2026-06-01T10:20:30"));
        assertEquals("2026-06-01 10:20:30", service.formatCellValue("updateTime", "2026-06-01T10:20:30+09:00"));
        assertEquals("not-a-date", service.formatCellValue("createTime", "not-a-date"));
    }

    @Test
    void displayCellValueMatchesWebPageFormatting() {
        assertEquals("\u306f\u3044", service.displayCellValue("goods", "isHot", Map.of("isHot", 1)));
        assertEquals("\u3044\u3044\u3048", service.displayCellValue("goods", "isHot", Map.of("isHot", 0)));
        assertEquals("12.5", service.displayCellValue("stockRecord", "changeQty", Map.of("changeQty", -12.50)));
        assertEquals(
                "\u7d0d\u54c1\u65e5: 2026-06-01 10:20:30",
                service.displayCellValue("stockOrder", "bizDate", Map.of(
                        "bizDate", "2026-06-01T10:20:30",
                        "orderType", 1)));
        assertEquals("\u6709\u52b9", service.displayCellValue("goods", "statusDesc", Map.of("statusDesc", "NORMAL")));
        assertEquals("\u7121\u52b9", service.displayCellValue("goods", "statusDesc", Map.of("statusDesc", "", "status", 0)));
        assertEquals("\u5165\u5eab", service.displayCellValue("stockOrder", "orderType", Map.of("orderType", 1)));
    }

    @Test
    void enumAndRecordIdHelpersHandleFallbacks() {
        assertEquals("1", service.normalizeEnumValue("ENABLE"));
        assertEquals("0", service.normalizeEnumValue("disable"));
        assertEquals("sku-7", service.resolveRecordId(Map.of("id", "goods-3", "skuId", "sku-7")));
        assertEquals("sku-7", service.resolveRecordId(Map.of("skuId", "sku-7")));
        assertEquals("", service.resolveRecordId(Map.of()));
    }

    @Test
    void rowHelpersCreateSelectionStateAndResolveCheckedRows() {
        JSONArray records = new JSONArray()
                .put(new JSONObject().put("id", 1).put("name", "first"))
                .put(new JSONObject().put("id", 2).put("name", "second"));

        List<Map<String, Object>> rows = service.createRows(records, "__selected");

        assertEquals(2, rows.size());
        assertEquals(Set.of("id", "name"), service.visibleKeys(rows, "__selected"));
        SimpleBooleanProperty selected = service.selectedProperty(rows.get(1), "__selected");
        assertFalse(selected.get());
        selected.set(true);
        assertEquals(List.of(rows.get(1)), service.checkedRows(rows, "__selected"));
        assertEquals(List.of("1", "2"), service.resolveRecordIds(rows));
        assertSame(rows.get(1), service.currentWorkingRow(null, rows, "__selected"));
        assertSame(rows.get(0), service.currentWorkingRow(rows.get(0), rows, "__selected"));
    }

    @Test
    void selectedPropertyRepairsMissingSelectionState() {
        Map<String, Object> row = new LinkedHashMap<>();

        SimpleBooleanProperty selected = service.selectedProperty(row, "__selected");

        assertFalse(selected.get());
        assertSame(selected, row.get("__selected"));
        assertTrue(service.checkedRows(null, "__selected").isEmpty());
    }
}
