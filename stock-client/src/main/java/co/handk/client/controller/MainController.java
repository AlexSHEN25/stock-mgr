package co.handk.client.controller;

import co.handk.client.MainApp;
import co.handk.client.model.Session;
import co.handk.client.util.ApiClient;
import co.handk.client.util.ModuleMeta;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class MainController {

    @FXML private Label currentUserLabel;
    @FXML private Label pageTitleLabel;
    @FXML private Label pageInfoLabel;
    @FXML private Label messageLabel;
    @FXML private TextField deleteIdField;
    @FXML private ComboBox<LanguageOption> languageCombo;
    @FXML private TableView<Map<String, Object>> dataTable;
    @FXML private FlowPane queryFieldsPane;

    private MainApp app;
    private String currentModule = "user";
    private int pageNum = 1;
    private final int pageSize = 10;
    private final Map<String, Control> queryControls = new LinkedHashMap<>();
    private String currentLang = "ja-JP";
    private Map<String, Object> inlineEditingRow;
    private Map<String, Object> inlineBackup;

    public void setApp(MainApp app) {
        this.app = app;
        initLanguageCombo();
        currentUserLabel.setText("Current User: " + Session.getUsername());
        dataTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        dataTable.setEditable(true);
        rebuildQueryFields();
        loadData();
    }

    private void initLanguageCombo() {
        if (languageCombo == null) {
            return;
        }
        languageCombo.getItems().setAll(
                new LanguageOption("zh-CN", "ZH"),
                new LanguageOption("ja-JP", "JA"),
                new LanguageOption("en-US", "EN")
        );
        LanguageOption selected = languageCombo.getItems().stream()
                .filter(option -> option.code().equalsIgnoreCase(currentLang))
                .findFirst()
                .orElse(languageCombo.getItems().get(0));
        languageCombo.setValue(selected);
    }

    @FXML
    private void onLanguageChange() {
        if (languageCombo == null) {
            return;
        }
        LanguageOption selected = languageCombo.getValue();
        if (selected == null) {
            return;
        }
        currentLang = selected.code();
        loadData();
    }

    @FXML
    private void onNavSelect(ActionEvent event) {
        if (!(event.getSource() instanceof Button btn)) {
            return;
        }
        switchModule(String.valueOf(btn.getUserData()), btn.getText());
    }

    @FXML
    private void onRefresh() {
        resetQueryControls();
        pageNum = 1;
        loadData();
    }

    @FXML
    private void onSearch() {
        pageNum = 1;
        loadData();
    }

    @FXML
    private void onPrevPage() {
        if (pageNum > 1) {
            pageNum--;
            loadData();
        }
    }

    @FXML
    private void onNextPage() {
        pageNum++;
        loadData();
    }

    @FXML
    private void onAdd() {
        openFormDialog("Create", false);
    }

    @FXML
    private void onInlineEdit() {
        Map<String, Object> selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Please select a row first.");
            return;
        }
        inlineEditingRow = selected;
        inlineBackup = new LinkedHashMap<>(selected);
        messageLabel.setText("Inline edit enabled for selected row.");
    }

    @FXML
    private void onInlineSave() {
        if (inlineEditingRow == null) {
            messageLabel.setText("No row is in inline edit mode.");
            return;
        }
        try {
            JSONObject dto = normalizeRowForUpdate(inlineEditingRow);
            String id = String.valueOf(dto.get("id"));
            String res = "user".equals(currentModule)
                    ? ApiClient.put("/user/" + id, dto.toString())
                    : ApiClient.put("/" + currentModule, dto.toString());
            JSONObject json = new JSONObject(res);
            if (isSuccess(json)) {
                messageLabel.setText("Inline update success.");
                inlineEditingRow = null;
                inlineBackup = null;
                loadData();
            } else {
                messageLabel.setText(json.optString("message", "Inline update failed."));
            }
        } catch (Exception ex) {
            messageLabel.setText("Inline update failed: " + ex.getMessage());
        }
    }

    @FXML
    private void onInlineCancel() {
        if (inlineEditingRow == null || inlineBackup == null) {
            messageLabel.setText("No row is in inline edit mode.");
            return;
        }
        inlineEditingRow.clear();
        inlineEditingRow.putAll(inlineBackup);
        inlineEditingRow = null;
        inlineBackup = null;
        dataTable.refresh();
        messageLabel.setText("Inline edit canceled.");
    }

    @FXML
    private void onEdit() {
        if (dataTable.getSelectionModel().getSelectedItem() == null) {
            messageLabel.setText("Please select a row first.");
            return;
        }
        openFormDialog("Edit", true);
    }

    @FXML
    private void onBatchDelete() {
        ObservableList<Map<String, Object>> selectedRows = dataTable.getSelectionModel().getSelectedItems();
        if (selectedRows == null || selectedRows.isEmpty()) {
            messageLabel.setText("Please select rows to delete.");
            return;
        }

        int ok = 0;
        for (Map<String, Object> row : selectedRows) {
            String id = resolveRecordId(row);
            if (id == null || id.isBlank()) {
                continue;
            }
            try {
                String res = ApiClient.delete("/" + currentModule + "/" + id);
                JSONObject json = new JSONObject(res);
                if (isSuccess(json)) {
                    ok++;
                }
            } catch (Exception ignored) {
            }
        }
        messageLabel.setText("Batch delete done. success=" + ok);
        loadData();
    }

    @FXML
    private void onDelete() {
        String id = deleteIdField.getText();
        if (id == null || id.isBlank()) {
            messageLabel.setText("Please input ID.");
            return;
        }

        try {
            String res = ApiClient.delete("/" + currentModule + "/" + id.trim());
            JSONObject json = new JSONObject(res);
            if (isSuccess(json)) {
                messageLabel.setText("Delete success.");
                loadData();
            } else {
                messageLabel.setText(json.optString("message", "Delete failed."));
            }
        } catch (Exception ex) {
            messageLabel.setText("Delete failed: " + ex.getMessage());
        }
    }

    @FXML
    private void onLogout() {
        try {
            String res = ApiClient.post("/user/logout", "{}");
            JSONObject json = new JSONObject(res);
            if (isSuccess(json)) {
                Session.clear();
                app.showLogin();
            } else {
                messageLabel.setText(json.optString("message", "Logout failed."));
            }
        } catch (Exception ex) {
            messageLabel.setText("Logout failed: " + ex.getMessage());
        }
    }

    private String resolveRecordId(Map<String, Object> row) {
        Object id = row.get("id");
        if (id == null) {
            id = row.get("skuId");
        }
        return id == null ? "" : String.valueOf(id);
    }

    private void switchModule(String module, String title) {
        currentModule = module;
        pageNum = 1;
        pageTitleLabel.setText(title);
        inlineEditingRow = null;
        inlineBackup = null;
        rebuildQueryFields();
        loadData();
    }

    private void rebuildQueryFields() {
        queryFieldsPane.getChildren().clear();
        queryControls.clear();

        for (String field : ModuleMeta.queryFields(currentModule)) {
            Label label = new Label(ModuleMeta.normalizeTitle(field));
            label.setStyle("-fx-text-fill:#4b5563;");
            Control control = createControl(field);
            control.setUserData(field);
            control.setPrefWidth(170);

            queryFieldsPane.getChildren().add(label);
            queryFieldsPane.getChildren().add(control);
            queryControls.put(field, control);
        }
    }

    private Control createControl(String field) {
        ModuleMeta.FieldType type = ModuleMeta.fieldType(currentModule, field);
        if ("status".equals(field) || type == ModuleMeta.FieldType.SELECT) {
            ComboBox<Option> combo = new ComboBox<>();
            combo.getItems().addAll(new Option("Enabled", "1"), new Option("Disabled", "0"));
            return combo;
        }

        String relationField = ModuleMeta.mapNameFieldToIdField(field);
        if (!relationField.isBlank()) {
            field = relationField;
            type = ModuleMeta.FieldType.RELATION;
        }

        if (type == ModuleMeta.FieldType.RELATION) {
            ComboBox<Option> combo = new ComboBox<>();
            String relationModule = ModuleMeta.relationModuleByField(field);
            if (relationModule != null) {
                combo.getItems().addAll(fetchRelationOptions(relationModule));
            }
            return combo;
        }

        return new TextField();
    }

    private List<Option> fetchRelationOptions(String module) {
        List<Option> options = new ArrayList<>();
        try {
            String res;
            if ("user".equals(module)) {
                JSONObject body = new JSONObject();
                body.put("pageNum", 1);
                body.put("pageSize", 50);
                res = ApiClient.post("/user/page", body.toString());
            } else {
                Map<String, String> params = new LinkedHashMap<>();
                params.put("pageNum", "1");
                params.put("pageSize", "50");
                res = ApiClient.get("/" + module + "/page", params);
            }
            JSONObject wrapper = new JSONObject(res);
            JSONObject data = wrapper.optJSONObject("data");
            if (data == null) {
                return options;
            }
            JSONArray records = data.optJSONArray("records");
            if (records == null) {
                return options;
            }
            for (int i = 0; i < records.length(); i++) {
                JSONObject r = records.getJSONObject(i);
                String id = String.valueOf(r.opt("id"));
                String label = r.optString("name", r.optString("username", r.optString("code", "ID:" + id)));
                options.add(new Option(label, id));
            }
        } catch (Exception ignored) {
        }
        return options;
    }

    private void resetQueryControls() {
        for (Control control : queryControls.values()) {
            if (control instanceof TextField tf) {
                tf.clear();
            } else if (control instanceof ComboBox<?> cb) {
                cb.setValue(null);
            }
        }
    }

    private Map<String, String> buildQueryParams() {
        Map<String, String> params = new LinkedHashMap<>();
        for (Map.Entry<String, Control> entry : queryControls.entrySet()) {
            String field = entry.getKey();
            Control control = entry.getValue();
            String value = readControlValue(control);
            if (value == null || value.isBlank()) {
                continue;
            }
            String mapId = ModuleMeta.mapNameFieldToIdField(field);
            params.put(mapId.isBlank() ? field : mapId, value);
        }
        return params;
    }

    private String readControlValue(Control control) {
        if (control instanceof TextField tf) {
            return tf.getText();
        }
        if (control instanceof ComboBox<?> cb) {
            Object val = cb.getValue();
            if (val instanceof Option op) {
                return op.value;
            }
            return val == null ? "" : String.valueOf(val);
        }
        return "";
    }

    private void openFormDialog(String action, boolean editMode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/module-form.fxml"));
            Parent root = loader.load();
            Stage modal = new Stage();
            modal.setTitle(action + " " + pageTitleLabel.getText());
            modal.initModality(Modality.WINDOW_MODAL);
            Window owner = dataTable != null && dataTable.getScene() != null ? dataTable.getScene().getWindow() : null;
            if (owner != null) {
                modal.initOwner(owner);
            }
            modal.setScene(new Scene(root));

            ModuleFormController formController = loader.getController();
            Map<String, Object> selected = editMode ? dataTable.getSelectionModel().getSelectedItem() : null;
            formController.configure(currentModule, action + " " + pageTitleLabel.getText(), editMode, selected);
            modal.setOnCloseRequest(e -> formController.markCanceled());
            modal.showAndWait();

            if (!formController.isSubmitted()) {
                return;
            }
            submitForm(editMode, formController.toJson());
        } catch (Exception ex) {
            messageLabel.setText("Form open failed: " + ex.getMessage());
        }
    }

    private void submitForm(boolean editMode, JSONObject dto) {
        try {
            String res;
            if (editMode) {
                String id = String.valueOf(dto.get("id"));
                res = "user".equals(currentModule)
                        ? ApiClient.put("/user/" + id, dto.toString())
                        : ApiClient.put("/" + currentModule, dto.toString());
            } else {
                res = ApiClient.post("/" + currentModule, dto.toString());
            }

            JSONObject json = new JSONObject(res);
            if (isSuccess(json)) {
                messageLabel.setText((editMode ? "Edit" : "Create") + " success.");
                loadData();
            } else {
                messageLabel.setText(json.optString("message", (editMode ? "Edit" : "Create") + " failed."));
            }
        } catch (Exception ex) {
            messageLabel.setText("Request failed: " + ex.getMessage());
        }
    }

    private JSONObject normalizeRowForUpdate(Map<String, Object> row) {
        JSONObject dto = new JSONObject();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith("Name") || key.endsWith("Desc") || "createTime".equals(key) || "updateTime".equals(key)) {
                continue;
            }
            Object val = entry.getValue();
            if (val == null) {
                continue;
            }
            if (val instanceof String s) {
                String text = s.trim();
                if (text.isEmpty()) {
                    continue;
                }
                ModuleMeta.FieldType type = ModuleMeta.fieldType(currentModule, key);
                try {
                    if (type == ModuleMeta.FieldType.NUMBER) {
                        if (text.contains(".")) {
                            dto.put(key, Double.parseDouble(text));
                        } else {
                            dto.put(key, Long.parseLong(text));
                        }
                    } else {
                        dto.put(key, text);
                    }
                } catch (Exception ex) {
                    dto.put(key, text);
                }
            } else {
                dto.put(key, val);
            }
        }
        return dto;
    }

    private boolean isSuccess(JSONObject json) {
        int code = json.optInt("code", -1);
        return code == 200 || code == 0;
    }

    private void loadData() {
        try {
            String res;
            if ("user".equals(currentModule)) {
                JSONObject body = new JSONObject();
                body.put("pageNum", pageNum);
                body.put("pageSize", pageSize);
                buildQueryParams().forEach(body::put);
                res = ApiClient.post("/user/page", body.toString());
            } else {
                Map<String, String> params = new LinkedHashMap<>();
                params.put("pageNum", String.valueOf(pageNum));
                params.put("pageSize", String.valueOf(pageSize));
                params.putAll(buildQueryParams());
                res = ApiClient.get("/" + currentModule + "/page", params);
            }

            JSONObject wrapper = new JSONObject(res);
            if (!isSuccess(wrapper)) {
                messageLabel.setText(wrapper.optString("message", "Load failed."));
                return;
            }

            JSONObject data = wrapper.optJSONObject("data");
            if (data == null) {
                messageLabel.setText("Response data is empty.");
                return;
            }

            long total = data.optLong("total", 0);
            long totalPages = data.optLong("totalPages", 0);
            pageInfoLabel.setText(String.format("Page %d / %d, Total %d", pageNum, totalPages, total));

            JSONArray records = data.optJSONArray("records");
            buildTable(records == null ? new JSONArray() : records);
            messageLabel.setText("Load success.");

            if (totalPages > 0 && pageNum > totalPages) {
                pageNum = (int) totalPages;
                loadData();
            }
        } catch (Exception ex) {
            messageLabel.setText("Load failed: " + ex.getMessage());
        }
    }

    private void buildTable(JSONArray records) {
        dataTable.getColumns().clear();

        ObservableList<Map<String, Object>> rows = FXCollections.observableArrayList();
        LinkedHashSet<String> keys = new LinkedHashSet<>();

        for (int i = 0; i < records.length(); i++) {
            JSONObject item = records.getJSONObject(i);
            Map<String, Object> row = new LinkedHashMap<>();
            for (String key : item.keySet()) {
                Object value = item.opt(key);
                row.put(key, value);
                keys.add(key);
            }
            rows.add(row);
        }

        for (String key : keys) {
            TableColumn<Map<String, Object>, String> col = new TableColumn<>(ModuleMeta.normalizeTitle(key));
            col.setEditable(true);
            col.setCellFactory(TextFieldTableCell.forTableColumn());
            col.setCellValueFactory(cell -> {
                Object v = cell.getValue().getOrDefault(key, "");
                if ("status".equals(key)) {
                    String s = String.valueOf(v);
                    if ("1".equals(s) || "ENABLE".equalsIgnoreCase(s)) {
                        return new SimpleStringProperty("Enabled");
                    }
                    if ("0".equals(s) || "DISABLE".equalsIgnoreCase(s)) {
                        return new SimpleStringProperty("Disabled");
                    }
                }
                return new SimpleStringProperty(String.valueOf(v));
            });
            col.setOnEditCommit(evt -> {
                Map<String, Object> row = evt.getRowValue();
                if (row == null) {
                    return;
                }
                String val = evt.getNewValue();
                if ("status".equals(key)) {
                    if ("Enabled".equals(val) || "1".equals(val) || "ENABLE".equalsIgnoreCase(val)) {
                        row.put(key, "ENABLE");
                    } else if ("Disabled".equals(val) || "0".equals(val) || "DISABLE".equalsIgnoreCase(val)) {
                        row.put(key, "DISABLE");
                    } else {
                        row.put(key, val);
                    }
                } else {
                    row.put(key, val);
                }
                dataTable.refresh();
            });
            dataTable.getColumns().add(col);
        }

        dataTable.setItems(rows);
    }

    private static final class Option {
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
    }

    private record LanguageOption(String code, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
