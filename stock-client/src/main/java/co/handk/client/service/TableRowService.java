package co.handk.client.service;

import co.handk.client.constant.AppConstants.Field;
import co.handk.client.util.ModuleMeta;
import javafx.beans.property.SimpleBooleanProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TableRowService {

    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger LOGGER = Logger.getLogger(TableRowService.class.getName());

    public List<Map<String, Object>> createRows(JSONArray records, String selectedKey) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < records.length(); i++) {
            JSONObject item = records.getJSONObject(i);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(selectedKey, new SimpleBooleanProperty(false));
            for (String key : item.keySet()) {
                row.put(key, item.opt(key));
            }
            rows.add(row);
        }
        return rows;
    }

    public LinkedHashSet<String> visibleKeys(List<Map<String, Object>> rows, String selectedKey) {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        for (Map<String, Object> row : rows) {
            for (String key : row.keySet()) {
                if (!selectedKey.equals(key)) {
                    keys.add(key);
                }
            }
        }
        return keys;
    }

    public JSONObject normalizeForUpdate(String module, Map<String, Object> row, String selectedKey) {
        JSONObject dto = new JSONObject();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String key = entry.getKey();
            if (selectedKey.equals(key)) {
                continue;
            }
            String payloadKey = ModuleMeta.updatePayloadField(module, key);
            if (payloadKey.isBlank()) {
                continue;
            }
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            value = normalizePayloadValue(module, payloadKey, value);
            if (value == null) {
                continue;
            }
            if (value instanceof String textValue) {
                String text = textValue.trim();
                if (text.isEmpty()) {
                    continue;
                }
                if (ModuleMeta.fieldType(module, payloadKey) == ModuleMeta.FieldType.NUMBER) {
                    try {
                        if (text.contains(".")) {
                            dto.put(payloadKey, Double.parseDouble(text));
                        } else {
                            dto.put(payloadKey, Long.parseLong(text));
                        }
                        continue;
                    } catch (NumberFormatException ex) {
                        LOGGER.log(Level.WARNING,
                                "Numeric parse fallback. module=" + module + ", field=" + payloadKey + ", value=" + text,
                                ex);
                    }
                }
                dto.put(payloadKey, text);
                continue;
            }
            dto.put(payloadKey, value);
        }
        return ModuleMeta.applyFormValueRules(module, dto);
    }

    private Object normalizePayloadValue(String module, String payloadKey, Object value) {
        ModuleMeta.FieldType fieldType = ModuleMeta.fieldType(module, payloadKey);
        if (value instanceof Boolean booleanValue) {
            if (fieldType == ModuleMeta.FieldType.SELECT || fieldType == ModuleMeta.FieldType.NUMBER) {
                return booleanValue ? 1 : 0;
            }
            return null;
        }
        if (fieldType == ModuleMeta.FieldType.SELECT) {
            String text = String.valueOf(value).trim();
            if ("true".equalsIgnoreCase(text)) {
                return 1;
            }
            if ("false".equalsIgnoreCase(text)) {
                return 0;
            }
            return normalizeEnumValue(text);
        }
        return value;
    }

    public String formatCellValue(String key, Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        if (text.isBlank()) {
            return "";
        }
        String lower = key == null ? "" : key.toLowerCase();
        if (lower.contains("time") || lower.contains("date")) {
            return tryFormatDateTime(key, text);
        }
        return text;
    }

    public String displayCellValue(String module, String key, Map<String, Object> row) {
        Object value = row.getOrDefault(key, "");
        if ("statusDesc".equalsIgnoreCase(key)) {
            return normalizeStatusDescription(value, row.get(Field.STATUS));
        }
        if ("isHot".equalsIgnoreCase(key)) {
            return "1".equals(String.valueOf(value)) ? "はい" : "いいえ";
        }
        if ("changeQty".equalsIgnoreCase(key)) {
            return formatAbsoluteNumber(value);
        }
        if ("bizDate".equalsIgnoreCase(key)) {
            return formatBizDate(value, row.get("orderType"));
        }
        String normalized = normalizeEnumValue(String.valueOf(value));
        ModuleMeta.Option option = ModuleMeta.optionByValue(module, key, normalized);
        if (option != null) {
            return option.label;
        }
        return formatCellValue(key, value);
    }

    public String normalizeEnumValue(String value) {
        if (value == null) {
            return "";
        }
        if ("ENABLE".equalsIgnoreCase(value)) {
            return "1";
        }
        if ("DISABLE".equalsIgnoreCase(value)) {
            return "0";
        }
        if ("true".equalsIgnoreCase(value)) {
            return "1";
        }
        if ("false".equalsIgnoreCase(value)) {
            return "0";
        }
        return value;
    }

    public String resolveRecordId(Map<String, Object> row) {
        Object id = row.get(Field.SKU_ID);
        if (id == null) {
            id = row.get(Field.ID);
        }
        return id == null ? "" : String.valueOf(id);
    }

    public List<String> resolveRecordIds(List<Map<String, Object>> rows) {
        List<String> ids = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            ids.add(resolveRecordId(row));
        }
        return ids;
    }

    public SimpleBooleanProperty selectedProperty(Map<String, Object> row, String selectedKey) {
        Object selected = row.get(selectedKey);
        if (selected instanceof SimpleBooleanProperty property) {
            return property;
        }
        SimpleBooleanProperty property = new SimpleBooleanProperty(false);
        row.put(selectedKey, property);
        return property;
    }

    public List<Map<String, Object>> checkedRows(Iterable<Map<String, Object>> rows, String selectedKey) {
        List<Map<String, Object>> checkedRows = new ArrayList<>();
        if (rows == null) {
            return checkedRows;
        }
        for (Map<String, Object> row : rows) {
            if (selectedProperty(row, selectedKey).get()) {
                checkedRows.add(row);
            }
        }
        return checkedRows;
    }

    public Map<String, Object> currentWorkingRow(
            Map<String, Object> selectedRow,
            Iterable<Map<String, Object>> rows,
            String selectedKey) {
        if (selectedRow != null) {
            return selectedRow;
        }
        List<Map<String, Object>> checkedRows = checkedRows(rows, selectedKey);
        return checkedRows.isEmpty() ? null : checkedRows.get(0);
    }

    private String tryFormatDateTime(String key, String text) {
        try {
            return LocalDateTime.parse(text).format(DISPLAY_TIME);
        } catch (DateTimeParseException ex) {
            LOGGER.log(Level.FINE, "Local date-time parse failed. field=" + key + ", value=" + text, ex);
        }
        try {
            return OffsetDateTime.parse(text).toLocalDateTime().format(DISPLAY_TIME);
        } catch (DateTimeParseException ex) {
            LOGGER.log(Level.FINE, "Offset date-time parse failed. field=" + key + ", value=" + text, ex);
            return text;
        }
    }

    private String normalizeStatusDescription(Object value, Object status) {
        String text = value == null ? "" : String.valueOf(value).trim();
        if ("NORMAL".equalsIgnoreCase(text) || "ON".equalsIgnoreCase(text)) {
            return "有効";
        }
        if ("OFF".equalsIgnoreCase(text)) {
            return "無効";
        }
        if (!text.isEmpty()) {
            return text;
        }
        return "1".equals(String.valueOf(status)) ? "有効" : "無効";
    }

    private String formatAbsoluteNumber(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return "";
        }
        try {
            return new BigDecimal(String.valueOf(value)).abs().stripTrailingZeros().toPlainString();
        } catch (NumberFormatException ex) {
            LOGGER.log(Level.FINE, "Quantity parse failed. value=" + value, ex);
            return String.valueOf(value);
        }
    }

    private String formatBizDate(Object value, Object orderType) {
        String formatted = formatCellValue("bizDate", value);
        if (formatted.isEmpty()) {
            return formatted;
        }
        if ("1".equals(String.valueOf(orderType))) {
            return "納品日: " + formatted;
        }
        if ("2".equals(String.valueOf(orderType))) {
            return "出荷日: " + formatted;
        }
        return formatted;
    }
}
