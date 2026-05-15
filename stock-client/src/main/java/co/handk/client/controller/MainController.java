package co.handk.client.controller;

import co.handk.client.MainApp;
import co.handk.client.model.Session;
import co.handk.client.util.ApiClient;
import co.handk.client.util.LanguageConfig;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class MainController {

    @FXML
    private Label currentUserLabel;
    @FXML
    private Label pageTitleLabel;
    @FXML
    private Label pageInfoLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private TextField keywordField;
    @FXML
    private TextField deleteIdField;
    @FXML
    private ComboBox<LanguageOption> languageCombo;
    @FXML
    private TableView<Map<String, Object>> dataTable;

    private MainApp app;
    private String currentModule = "user";
    private int pageNum = 1;
    private final int pageSize = 10;

    public void setApp(MainApp app) {
        this.app = app;
        initLanguageCombo();
        currentUserLabel.setText("Current User: " + Session.getUsername());
        loadData();
    }

    private void initLanguageCombo() {
        languageCombo.getItems().setAll(
                new LanguageOption("ja-JP", "日本語"),
                new LanguageOption("zh-CN", "简体中文"),
                new LanguageOption("en-US", "English")
        );
        String configured = LanguageConfig.getLanguage();
        LanguageOption selected = languageCombo.getItems().stream()
                .filter(option -> option.code().equalsIgnoreCase(configured))
                .findFirst()
                .orElse(languageCombo.getItems().get(0));
        languageCombo.setValue(selected);
        languageCombo.setOnAction(event -> onLanguageChange());
    }

    @FXML
    private void onLanguageChange() {
        LanguageOption selected = languageCombo.getValue();
        if (selected == null) {
            return;
        }
        LanguageConfig.setLanguage(selected.code());
        loadData();
    }

    @FXML
    private void onUserModule() {
        switchModule("user", "User Management");
    }

    @FXML
    private void onStockModule() {
        switchModule("stock", "Stock Management");
    }

    @FXML
    private void onWarehouseModule() {
        switchModule("warehouse", "Warehouse Management");
    }

    @FXML
    private void onStockTypeModule() {
        switchModule("stockType", "Stock Type Management");
    }

    @FXML
    private void onRefresh() {
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
    private void onEdit() {
        if (dataTable.getSelectionModel().getSelectedItem() == null) {
            messageLabel.setText("Please select one row to edit");
            return;
        }
        openFormDialog("Edit", true);
    }

    @FXML
    private void onDelete() {
        String id = deleteIdField.getText();
        if (id == null || id.isBlank()) {
            messageLabel.setText("Please input ID to delete");
            return;
        }

        try {
            String res = ApiClient.delete("/" + currentModule + "/" + id.trim());
            JSONObject json = new JSONObject(res);
            if (json.optInt("code") == 0) {
                messageLabel.setText("Delete success");
                loadData();
            } else {
                messageLabel.setText(json.optString("message", "Delete failed"));
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
            if (json.optInt("code") == 0) {
                Session.clear();
                app.showLogin();
            } else {
                messageLabel.setText(json.optString("message", "Logout failed"));
            }
        } catch (Exception ex) {
            messageLabel.setText("Logout failed: " + ex.getMessage());
        }
    }

    private void switchModule(String module, String title) {
        currentModule = module;
        pageNum = 1;
        keywordField.clear();
        pageTitleLabel.setText(title);
        loadData();
    }

    private void openFormDialog(String action, boolean editMode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/module-form.fxml"));
            DialogPane pane = new DialogPane();
            pane.setContent(loader.load());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(action + " " + pageTitleLabel.getText());
            dialog.getDialogPane().setContent(pane.getContent());
            dialog.getDialogPane().getButtonTypes().clear();
            dialog.show();

            ModuleFormController formController = loader.getController();
            Map<String, Object> selected = editMode ? dataTable.getSelectionModel().getSelectedItem() : null;
            formController.configure(currentModule, action + " " + pageTitleLabel.getText(), editMode, selected);

            dialog.setOnHidden(event -> {
                if (!formController.isSubmitted()) {
                    return;
                }
                submitForm(editMode, formController.toJson());
            });
        } catch (Exception ex) {
            messageLabel.setText("Open form failed: " + ex.getMessage());
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
            if (json.optInt("code") == 0) {
                messageLabel.setText((editMode ? "Edit" : "Create") + " success");
                loadData();
            } else {
                messageLabel.setText(json.optString("message", (editMode ? "Edit" : "Create") + " failed"));
            }
        } catch (Exception ex) {
            messageLabel.setText("Submit failed: " + ex.getMessage());
        }
    }

    private void loadData() {
        try {
            String res;
            if ("user".equals(currentModule)) {
                JSONObject body = new JSONObject();
                body.put("pageNum", pageNum);
                body.put("pageSize", pageSize);
                String keyword = keywordField.getText();
                if (keyword != null && !keyword.isBlank()) {
                    body.put("username", keyword.trim());
                }
                res = ApiClient.post("/user/page", body.toString());
            } else {
                Map<String, String> params = new LinkedHashMap<>();
                params.put("pageNum", String.valueOf(pageNum));
                params.put("pageSize", String.valueOf(pageSize));
                String keyword = keywordField.getText();
                if (keyword != null && !keyword.isBlank()) {
                    params.put("stock".equals(currentModule) ? "goodsName" : "name", keyword.trim());
                }
                res = ApiClient.get("/" + currentModule + "/page", params);
            }

            JSONObject wrapper = new JSONObject(res);
            if (wrapper.optInt("code") != 0) {
                messageLabel.setText(wrapper.optString("message", "Query failed"));
                return;
            }

            JSONObject data = wrapper.optJSONObject("data");
            if (data == null) {
                messageLabel.setText("Response data invalid");
                return;
            }

            long total = data.optLong("total", 0);
            long totalPages = data.optLong("totalPages", 0);
            pageInfoLabel.setText(String.format("Page %d / %d, Total %d", pageNum, totalPages, total));

            JSONArray records = data.optJSONArray("records");
            buildTable(records == null ? new JSONArray() : records);
            messageLabel.setText("Load success");

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
            TableColumn<Map<String, Object>, String> col = new TableColumn<>(key);
            col.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getOrDefault(key, ""))));
            dataTable.getColumns().add(col);
        }
        dataTable.setItems(rows);
    }

    private record LanguageOption(String code, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
