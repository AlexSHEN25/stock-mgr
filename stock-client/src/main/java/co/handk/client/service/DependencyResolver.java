package co.handk.client.service;

import co.handk.client.util.ModuleMeta;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class DependencyResolver {

    public interface OptionValue {
        String value();
    }

    public <T extends OptionValue> void bind(
            String module,
            Map<String, Control> controls,
            BiFunction<String, Map<String, String>, List<T>> optionLoader) {

        for (ModuleMeta.DependencyRule rule : ModuleMeta.dependencyRules(module)) {
            Control parentCtrl = controls.get(rule.parentField);
            Control childCtrl = controls.get(rule.childField);
            if (!(parentCtrl instanceof ComboBox<?>) || !(childCtrl instanceof ComboBox<?>)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            ComboBox<T> parentCombo = (ComboBox<T>) parentCtrl;
            @SuppressWarnings("unchecked")
            ComboBox<T> childCombo = (ComboBox<T>) childCtrl;

            parentCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                String parentValue = newVal == null ? "" : newVal.value();
                Map<String, String> filters = buildFilters(rule, controls, parentValue);
                childCombo.getItems().setAll(optionLoader.apply(rule.sourceModule, filters));
                childCombo.setValue(null);
                clearCascade(controls, rule.cascadeClearFields);
            });
        }
    }

    private Map<String, String> buildFilters(ModuleMeta.DependencyRule rule, Map<String, Control> controls, String parentValue) {
        if (parentValue == null || parentValue.isBlank()) {
            return Map.of();
        }
        Map<String, String> filters = new LinkedHashMap<>();
        filters.put(rule.queryParam, parentValue);
        for (Map.Entry<String, String> entry : rule.additionalQueryParams.entrySet()) {
            String value = controlValue(controls.get(entry.getValue()));
            if (value != null && !value.isBlank()) {
                filters.put(entry.getKey(), value);
            }
        }
        return filters;
    }

    private String controlValue(Control control) {
        if (control instanceof TextField tf) {
            return tf.getText();
        }
        if (control instanceof TextArea ta) {
            return ta.getText();
        }
        if (control instanceof ComboBox<?> combo) {
            Object value = combo.getValue();
            if (value instanceof OptionValue optionValue) {
                return optionValue.value();
            }
            return value == null ? "" : String.valueOf(value);
        }
        return "";
    }

    private void clearCascade(Map<String, Control> controls, List<String> fields) {
        for (String field : fields) {
            Control c = controls.get(field);
            if (c instanceof TextField tf) {
                tf.clear();
            } else if (c instanceof TextArea ta) {
                ta.clear();
            } else if (c instanceof ComboBox<?> cb) {
                cb.setValue(null);
                cb.getItems().clear();
            }
        }
    }
}
