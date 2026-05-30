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
    private final ModuleDataService dataService = new ModuleDataService();
    private final DependencyResolver dependencyResolver = new DependencyResolver();

    public void configure(String module, String title, boolean editMode, Map<String, Object> source) {
        this.module = module;
        this.editMode = editMode;
        this.sourceValues = source == null ? Map.of() : source;
        this.titleLabel.setText(title);

        buildDynamicControls(module, editMode);
        bindDependencyRules();
        fillValues(source);
        rawJsonArea.setText(toJsonFromControls().toString(2));
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void markCanceled() {
        submitted = false;
    }

    public JSONObject toJson() {
        String raw = rawJsonArea.getText();
        if (raw != null && !raw.isBlank()) {
            return new JSONObject(raw);
        }
        return toJsonFromControls();
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
            for (String field : ModuleMeta.formFields(module)) {
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
        fields.addAll(ModuleMeta.formFields(module));

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
        if (ModuleMeta.fieldType(module, field) == ModuleMeta.FieldType.RELATION) {
            ComboBox<Option> combo = new ComboBox<>();
            combo.setEditable(false);
            combo.getItems().addAll(fetchRelationOptions(ModuleMeta.relationModuleByField(field), Map.of()));
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
                ta.setText(String.valueOf(val));
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
            Map<String, String> filters = Map.of(rule.queryParam, String.valueOf(parentVal));
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
