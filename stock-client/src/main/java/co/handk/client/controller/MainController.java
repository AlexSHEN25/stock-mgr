package co.handk.client.controller;

import co.handk.client.MainApp;
import co.handk.client.model.Session;
import co.handk.client.util.ApiClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

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
    private TableView<Map<String, Object>> dataTable;

    private MainApp app;
    private String currentModule = "user";
    private int pageNum = 1;
    private final int pageSize = 10;

    public void setApp(MainApp app) {
        this.app = app;
        currentUserLabel.setText("当前用户: " + Session.getUsername());
        loadData();
    }

    @FXML
    private void onUserModule() { switchModule("user", "用户管理"); }
    @FXML
    private void onStockModule() { switchModule("stock", "库存管理"); }
    @FXML
    private void onWarehouseModule() { switchModule("warehouse", "仓库管理"); }
    @FXML
    private void onStockTypeModule() { switchModule("stockType", "库存类型"); }

    @FXML
    private void onRefresh() { loadData(); }

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
        openFormDialog("新增", false);
    }

    @FXML
    private void onEdit() {
        if (dataTable.getSelectionModel().getSelectedItem() == null) {
            messageLabel.setText("请先选中一条记录再编辑");
            return;
        }
        openFormDialog("编辑", true);
    }

    @FXML
    private void onDelete() {
        String id = deleteIdField.getText();
        if (id == null || id.isBlank()) {
            messageLabel.setText("请输入要删除的ID");
            return;
        }

        try {
            String res = ApiClient.delete("/" + currentModule + "/" + id.trim());
            JSONObject json = new JSONObject(res);
            if (json.optInt("code") == 0) {
                messageLabel.setText("删除成功");
                loadData();
            } else {
                messageLabel.setText(json.optString("message", "删除失败"));
            }
        } catch (Exception ex) {
            messageLabel.setText("删除失败: " + ex.getMessage());
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
                messageLabel.setText(json.optString("message", "退出失败"));
            }
        } catch (Exception ex) {
            messageLabel.setText("退出失败: " + ex.getMessage());
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
            dialog.setTitle(action + pageTitleLabel.getText());
            dialog.getDialogPane().setContent(pane.getContent());
            dialog.getDialogPane().getButtonTypes().clear();
            dialog.show();

            ModuleFormController formController = loader.getController();
            Map<String, Object> selected = editMode ? dataTable.getSelectionModel().getSelectedItem() : null;
            formController.configure(currentModule, action + pageTitleLabel.getText(), editMode, selected);

            dialog.setOnHidden(event -> {
                if (!formController.isSubmitted()) {
                    return;
                }
                submitForm(editMode, formController.toJson());
            });
        } catch (Exception ex) {
            messageLabel.setText("打开表单失败: " + ex.getMessage());
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
                messageLabel.setText((editMode ? "编辑" : "新增") + "成功");
                loadData();
            } else {
                messageLabel.setText(json.optString("message", (editMode ? "编辑" : "新增") + "失败"));
            }
        } catch (Exception ex) {
            messageLabel.setText("提交失败: " + ex.getMessage());
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
                messageLabel.setText(wrapper.optString("message", "查询失败"));
                return;
            }

            JSONObject data = wrapper.optJSONObject("data");
            if (data == null) {
                messageLabel.setText("返回数据为空");
                return;
            }

            long total = data.optLong("total", 0);
            long totalPages = data.optLong("totalPages", 0);
            pageInfoLabel.setText(String.format("第 %d 页 / 共 %d 页 / 总计 %d 条", pageNum, totalPages, total));

            JSONArray records = data.optJSONArray("records");
            buildTable(records == null ? new JSONArray() : records);
            messageLabel.setText("加载成功");

            if (totalPages > 0 && pageNum > totalPages) {
                pageNum = (int) totalPages;
                loadData();
            }
        } catch (Exception ex) {
            messageLabel.setText("加载失败: " + ex.getMessage());
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
}
