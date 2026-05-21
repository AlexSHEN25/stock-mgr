package co.handk.client.service;

import co.handk.client.util.ModuleMeta;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;
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
                Map<String, String> filters = parentValue.isBlank() ? Map.of() : Map.of(rule.queryParam, parentValue);
                childCombo.getItems().setAll(optionLoader.apply(rule.sourceModule, filters));
                childCombo.setValue(null);
                clearCascade(controls, rule.cascadeClearFields);
            });
        }
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

