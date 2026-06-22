package co.handk.client.controller;

import co.handk.client.constant.UiText;
import co.handk.client.service.DependencyResolver;
import co.handk.client.service.ModuleDataService;
import co.handk.client.util.ModuleMeta;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModuleFormController {

    private static final Logger LOGGER = Logger.getLogger(ModuleFormController.class.getName());

    @FXML private Label titleLabel;
    @FXML private Label errorLabel;
    @FXML private GridPane dynamicFormGrid;
    @FXML private TextArea rawJsonArea;
    @FXML private Button saveButton;

    private boolean submitted;
    private String module;
    private boolean editMode;
    private final Map<String, Control> controls = new LinkedHashMap<>();
    private Map<String, Object> sourceValues = Map.of();
    private List<String> formFields = List.of();
    private String generatedRawJson = "";
    private final ModuleDataService dataService = new ModuleDataService();
    private final DependencyResolver dependencyResolver = new DependencyResolver();

    public void configure(String module, String title, boolean editMode, Map<String, Object> source) {
        configure(module, title, editMode, source, List.of());
    }

    public void configure(
            String module,
            String title,
            boolean editMode,
            Map<String, Object> source,
            Iterable<String> availableFields) {
        this.module = module;
        this.editMode = editMode;
        this.sourceValues = source == null ? Map.of() : source;
        Set<String> fallbackFields = new LinkedHashSet<>();
        if (availableFields != null) {
            availableFields.forEach(fallbackFields::add);
        }
        fallbackFields.addAll(this.sourceValues.keySet());
        this.formFields = ModuleMeta.resolvedFormFields(module, fallbackFields);
        this.titleLabel.setText(title);

        buildDynamicControls(module, editMode);
        bindDependencyRules();
        applyDefaultRelationValues();
        fillValues(source);
        generatedRawJson = toJsonFromControls().toString(2);
        rawJsonArea.setText(generatedRawJson);
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void markCanceled() {
        submitted = false;
    }

    public JSONObject toJson() {
        String raw = rawJsonArea.getText();
        JSONObject dto;
        if (raw != null && !raw.isBlank() && !raw.equals(generatedRawJson)) {
            dto = new JSONObject(raw);
        } else {
            dto = toJsonFromControls();
        }
        return applyModuleSpecificValues(dto);
    }

    @FXML
    private void onSave() {
        errorLabel.setText("");
        try {
            JSONObject dto = toJson();
            dto = ModuleMeta.applyFormValueRules(module, dto);
            if (editMode && !dto.has("id")) {
                errorLabel.setText(UiText.MSG_EDIT_ID_REQUIRED);
                return;
            }
            for (String field : formFields) {
                if (!ModuleMeta.isRequiredFormField(module, field)) {
                    continue;
                }
                Object v = dto.opt(field);
                if (v == null || (v instanceof String s && s.isBlank())) {
                    errorLabel.setText(ModuleMeta.normalizeTitle(field) + UiText.MSG_REQUIRED_SUFFIX);
                    return;
                }
            }
            submitted = true;
            saveButton.getScene().getWindow().hide();
        } catch (Exception ex) {
            errorLabel.setText(UiText.MSG_JSON_PARSE_FAIL + ex.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        submitted = false;
        saveButton.getScene().getWindow().hide();
    }

    private void buildDynamicControls(String module, boolean editMode) {
        dynamicFormGrid.getChildren().clear();
        controls.clear();

        Set<String> fields = new LinkedHashSet<>();
        if (editMode) {
            fields.add("id");
        }
        fields.addAll(formFields);

        int row = 0;
        for (String field : fields) {
            Label label = new Label(ModuleMeta.normalizeTitle(field));
            Control control = createControl(module, field);
            control.setPrefWidth(460);
            controls.put(field, control);

            dynamicFormGrid.add(label, 0, row);
            dynamicFormGrid.add(control, 1, row);
            row++;
        }
    }

    private Control createControl(String module, String field) {
        if (isJsonArrayField(field)) {
            TextArea area = new TextArea();
            area.setPrefRowCount("relationItems".equals(field) ? 6 : 2);
            return area;
        }
        if (ModuleMeta.fieldType(module, field) == ModuleMeta.FieldType.RELATION) {
            ComboBox<Option> combo = new ComboBox<>();
            combo.setEditable(false);
            combo.getItems().addAll(fetchRelationOptions(
                    ModuleMeta.relationModuleByField(field),
                    ModuleMeta.initialRelationFilters(module, field)));
            return combo;
        }

        if (ModuleMeta.fieldType(module, field) == ModuleMeta.FieldType.SELECT) {
            ComboBox<Option> combo = new ComboBox<>();
            List<ModuleMeta.Option> opts = ModuleMeta.selectOptions(module, field);
            for (ModuleMeta.Option opt : opts) {
                combo.getItems().add(new Option(opt.label, opt.value));
            }
            return combo;
        }

        String low = field.toLowerCase();
        if (low.contains("remark") || low.contains("content") || low.contains("description")) {
            TextArea area = new TextArea();
            area.setPrefRowCount(3);
            return area;
        }
        return new TextField();
    }

    private void bindDependencyRules() {
        dependencyResolver.bind(module, controls, this::fetchRelationOptions);
    }

    private void applyDefaultRelationValues() {
        if (editMode) {
            return;
        }
        for (Map.Entry<String, Control> entry : controls.entrySet()) {
            String field = entry.getKey();
            if (!ModuleMeta.shouldAutoSelectFirstRelation(module, field) || !(entry.getValue() instanceof ComboBox<?> combo)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            ComboBox<Option> typedCombo = (ComboBox<Option>) combo;
            if (typedCombo.getValue() != null || typedCombo.getItems().isEmpty()) {
                continue;
            }
            typedCombo.setValue(typedCombo.getItems().get(0));
        }
    }

    private List<Option> fetchRelationOptions(String relationModule, Map<String, String> extraFilters) {
        List<Option> options = new ArrayList<>();
        if (relationModule == null || relationModule.isBlank()) {
            return options;
        }
        try {
            for (Map<String, String> item : dataService.fetchSimpleRelationOptions(relationModule, extraFilters)) {
                options.add(new Option(item.get("label"), item.get("value")));
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Load relation options failed. module=" + module + ", relationModule=" + relationModule + ", filters=" + extraFilters, ex);
        }
        return options;
    }

    private void fillValues(Map<String, Object> source) {
        preloadDependencyChildren();

        if (source == null) {
            return;
        }
        for (Map.Entry<String, Object> e : source.entrySet()) {
            String key = e.getKey();
            Object val = e.getValue();
            Control c = controls.get(key);
            if (c == null || val == null) {
                continue;
            }
            if (c instanceof TextField tf) {
                tf.setText(String.valueOf(val));
            } else if (c instanceof TextArea ta) {
                ta.setText(formatTextAreaValue(key, val));
            } else if (c instanceof ComboBox<?> combo) {
                @SuppressWarnings("unchecked")
                ComboBox<Option> cb = (ComboBox<Option>) combo;
                Option selected = null;
                for (Option op : cb.getItems()) {
                    if (op.value.equals(String.valueOf(val))) {
                        selected = op;
                        break;
                    }
                }
                if (selected != null) {
                    cb.setValue(selected);
                } else {
                    Option fallback = new Option(String.valueOf(val), String.valueOf(val));
                    cb.getItems().add(fallback);
                    cb.setValue(fallback);
                }
            }
        }
    }

    private void preloadDependencyChildren() {
        for (ModuleMeta.DependencyRule rule : ModuleMeta.dependencyRules(module)) {
            Control parentCtrl = controls.get(rule.parentField);
            Control childCtrl = controls.get(rule.childField);
            if (!(parentCtrl instanceof ComboBox<?>) || !(childCtrl instanceof ComboBox<?>)) {
                continue;
            }

            Object parentVal = sourceValues.get(rule.parentField);
            if (parentVal == null || String.valueOf(parentVal).isBlank()) {
                continue;
            }
            @SuppressWarnings("unchecked")
            ComboBox<Option> childCombo = (ComboBox<Option>) childCtrl;
            Map<String, String> filters = new LinkedHashMap<>();
            filters.put(rule.queryParam, String.valueOf(parentVal));
            for (Map.Entry<String, String> entry : rule.additionalQueryParams.entrySet()) {
                Object extraValue = sourceValues.get(entry.getValue());
                if (extraValue != null && !String.valueOf(extraValue).isBlank()) {
                    filters.put(entry.getKey(), String.valueOf(extraValue));
                }
            }
            childCombo.getItems().setAll(fetchRelationOptions(rule.sourceModule, filters));
        }
    }

    private JSONObject toJsonFromControls() {
        JSONObject dto = new JSONObject();
        for (Map.Entry<String, Control> entry : controls.entrySet()) {
            String key = entry.getKey();
            Control c = entry.getValue();
            Object val = extractValue(c);
            if (val == null) {
                continue;
            }

            String text = String.valueOf(val).trim();
            if (text.isEmpty()) {
                continue;
            }

            if (isJsonArrayField(key)) {
                dto.put(key, parseJsonArrayField(key, text));
                continue;
            }

            if (ModuleMeta.fieldType(module, key) == ModuleMeta.FieldType.NUMBER) {
                try {
                    if (text.contains(".")) {
                        dto.put(key, Double.parseDouble(text));
                    } else {
                        dto.put(key, Long.parseLong(text));
                    }
                    continue;
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Numeric parse fallback. module=" + module + ", field=" + key + ", value=" + text, ex);
                }
            }
            dto.put(key, text);
        }
        return dto;
    }

    private JSONObject applyModuleSpecificValues(JSONObject dto) {
        if (dto == null) {
            return new JSONObject();
        }
        if ("stock".equals(module) && isBlankJsonValue(dto.opt("stockId"))) {
            Object stockId = sourceValues.get("stockId");
            if (stockId == null) {
                stockId = sourceValues.get("id");
            }
            if (stockId != null && !String.valueOf(stockId).isBlank()) {
                dto.put("stockId", stockId);
            }
        }
        if ("stock".equals(module)) {
            copyFromSourceIfBlank(dto, "goodsId");
            copyFromSourceIfBlank(dto, "skuId");
            copyFromSourceIfBlank(dto, "goodsName", "name");
            copyFromSourceIfBlank(dto, "skuCode");
            copyFromSourceIfBlank(dto, "brandId");
            copyFromSourceIfBlank(dto, "seriesId");
            copyFromSourceIfBlank(dto, "categoryId");
            copyFromSourceIfBlank(dto, "makerId");
            copyFromSourceIfBlank(dto, "deptId");
            copyFromSourceIfBlank(dto, "groupCode");
            copyFromSourceIfBlank(dto, "deptCode", "groupCode");
            if (isBlankJsonValue(dto.opt("outboundMode"))) {
                if (hasSourceValue("groupCode") || hasSourceValue("deptId")) {
                    dto.put("outboundMode", "GROUP_CUSTOMER");
                } else {
                    dto.put("outboundMode", "CUSTOMER");
                }
            }
        }
        return dto;
    }

    private void copyFromSourceIfBlank(JSONObject dto, String field) {
        copyFromSourceIfBlank(dto, field, field);
    }

    private void copyFromSourceIfBlank(JSONObject dto, String targetField, String sourceField) {
        if (!isBlankJsonValue(dto.opt(targetField))) {
            return;
        }
        Object value = sourceValues.get(sourceField);
        if (value == null || String.valueOf(value).isBlank()) {
            return;
        }
        dto.put(targetField, value);
    }

    private boolean isBlankJsonValue(Object value) {
        return value == null || String.valueOf(value).trim().isEmpty() || "null".equalsIgnoreCase(String.valueOf(value).trim());
    }

    private boolean hasSourceValue(String key) {
        Object value = sourceValues.get(key);
        return value != null && !String.valueOf(value).trim().isEmpty() && !"null".equalsIgnoreCase(String.valueOf(value).trim());
    }

    private Object extractValue(Control c) {
        if (c instanceof TextField tf) {
            return tf.getText();
        }
        if (c instanceof TextArea ta) {
            return ta.getText();
        }
        if (c instanceof ComboBox<?> combo) {
            Object selected = combo.getValue();
            if (selected instanceof Option op) {
                return op.value;
            }
            return selected == null ? "" : String.valueOf(selected);
        }
        return null;
    }

    private boolean isJsonArrayField(String field) {
        return "relationItems".equals(field) || field.endsWith("Ids");
    }

    private Object parseJsonArrayField(String field, String text) {
        if ("relationItems".equals(field)) {
            return new JSONArray(text);
        }
        JSONArray array = new JSONArray();
        String normalized = text.replace("\r", "\n");
        for (String token : normalized.split("[,\\n]")) {
            String value = token == null ? "" : token.trim();
            if (value.isEmpty()) {
                continue;
            }
            try {
                array.put(Long.parseLong(value));
            } catch (NumberFormatException ex) {
                array.put(value);
            }
        }
        return array;
    }

    private String formatTextAreaValue(String field, Object value) {
        if (value == null) {
            return "";
        }
        if ("relationItems".equals(field)) {
            if (value instanceof JSONArray jsonArray) {
                return jsonArray.toString(2);
            }
            if (value instanceof List<?> list) {
                return new JSONArray(list).toString(2);
            }
        }
        if (field.endsWith("Ids")) {
            if (value instanceof JSONArray jsonArray) {
                List<String> lines = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    lines.add(String.valueOf(jsonArray.opt(i)));
                }
                return String.join(", ", lines);
            }
            if (value instanceof List<?> list) {
                return String.join(", ", list.stream().map(String::valueOf).toList());
            }
        }
        return String.valueOf(value);
    }

    private static final class Option implements DependencyResolver.OptionValue {
        private final String label;
        private final String value;

        private Option(String label, String value) {
            this.label = label;
            this.value = value;
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public String value() {
            return value;
        }
    }
}
