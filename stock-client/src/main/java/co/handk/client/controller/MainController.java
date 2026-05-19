package co.handk.client.controller;

import co.handk.client.MainApp;
import co.handk.client.model.Session;
import co.handk.client.util.ApiClient;
import co.handk.client.util.ModuleMeta;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class MainController {

    private static final String SELECTED_KEY = "__selected";

    @FXML private Label currentUserLabel;
    @FXML private Label pageTitleLabel;
    @FXML private Label pageInfoLabel;
    @FXML private Label messageLabel;
    @FXML private TextField deleteIdField;
    @FXML private TableView<Map<String, Object>> dataTable;
    @FXML private FlowPane queryFieldsPane;

    private MainApp app;
    private String currentModule = "user";
    private int pageNum = 1;
    private final int pageSize = 10;
    private final Map<String, Control> queryControls = new LinkedHashMap<>();
    private Map<String, Object> inlineEditingRow;
    private Map<String, Object> inlineBackup;

    public void setApp(MainApp app) {
        this.app = app;
        currentUserLabel.setText("ログインユーザー: " + Session.getUsername());
        dataTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        dataTable.getSelectionModel().setCellSelectionEnabled(true);
        dataTable.setEditable(true);
        dataTable.setTableMenuButtonVisible(true);
        dataTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        rebuildQueryFields();
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
        openFormDialog("新規作成", false);
    }

    @FXML
    private void onInlineEdit() {
        Map<String, Object> selected = currentWorkingRow();
        if (selected == null) {
            messageLabel.setText("先に行を選択してください。");
            return;
        }
        inlineEditingRow = selected;
        inlineBackup = new LinkedHashMap<>(selected);
        inlineBackup.remove(SELECTED_KEY);
        messageLabel.setText("選択行のインライン編集を開始しました。");
    }

    @FXML
    private void onInlineSave() {
        if (inlineEditingRow == null) {
            messageLabel.setText("インライン編集対象がありません。");
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
                messageLabel.setText("インライン更新が完了しました。");
                inlineEditingRow = null;
                inlineBackup = null;
                loadData();
            } else {
                messageLabel.setText(json.optString("message", "インライン更新に失敗しました。"));
            }
        } catch (Exception ex) {
            messageLabel.setText("更新に失敗しました: " + ex.getMessage());
        }
    }

    @FXML
    private void onInlineCancel() {
        if (inlineEditingRow == null || inlineBackup == null) {
            messageLabel.setText("インライン編集対象がありません。");
            return;
        }
        SimpleBooleanProperty selectedProperty = selectedProperty(inlineEditingRow);
        inlineEditingRow.clear();
        inlineEditingRow.putAll(inlineBackup);
        inlineEditingRow.put(SELECTED_KEY, selectedProperty);
        inlineEditingRow = null;
        inlineBackup = null;
        dataTable.refresh();
        messageLabel.setText("インライン編集を取り消しました。");
    }

    @FXML
    private void onEdit() {
        if (currentWorkingRow() == null) {
            messageLabel.setText("先に行を選択してください。");
            return;
        }
        openFormDialog("編集", true);
    }

    @FXML
    private void onBatchDelete() {
        List<Map<String, Object>> checkedRows = checkedRows();
        if (checkedRows.isEmpty()) {
            messageLabel.setText("削除する行にチェックを入れてください。");
            return;
        }

        int ok = 0;
        for (Map<String, Object> row : checkedRows) {
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
        messageLabel.setText("一括削除完了: 成功 " + ok + " 件");
        loadData();
    }

    @FXML
    private void onDelete() {
        String id = deleteIdField.getText();
        if (id == null || id.isBlank()) {
            messageLabel.setText("IDを入力してください。");
            return;
        }

        try {
            String res = ApiClient.delete("/" + currentModule + "/" + id.trim());
            JSONObject json = new JSONObject(res);
            if (isSuccess(json)) {
                messageLabel.setText("削除に成功しました。");
                loadData();
            } else {
                messageLabel.setText(json.optString("message", "削除に失敗しました。"));
            }
        } catch (Exception ex) {
            messageLabel.setText("削除に失敗しました: " + ex.getMessage());
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
                messageLabel.setText(json.optString("message", "ログアウトに失敗しました。"));
            }
        } catch (Exception ex) {
            messageLabel.setText("ログアウトに失敗しました: " + ex.getMessage());
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
            combo.getItems().addAll(new Option("有効", "1"), new Option("無効", "0"));
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
            Map<String, Object> selected = editMode ? currentWorkingRow() : null;
            formController.configure(currentModule, action + " " + pageTitleLabel.getText(), editMode, selected);
            modal.setOnCloseRequest(e -> formController.markCanceled());
            modal.showAndWait();

            if (!formController.isSubmitted()) {
                return;
            }
            submitForm(editMode, formController.toJson());
        } catch (Exception ex) {
            messageLabel.setText("フォーム表示に失敗しました: " + ex.getMessage());
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
                messageLabel.setText(editMode ? "更新に成功しました。" : "作成に成功しました。");
                loadData();
            } else {
                messageLabel.setText(json.optString("message", editMode ? "更新に失敗しました。" : "作成に失敗しました。"));
            }
        } catch (Exception ex) {
            messageLabel.setText("保存に失敗しました: " + ex.getMessage());
        }
    }

    private JSONObject normalizeRowForUpdate(Map<String, Object> row) {
        JSONObject dto = new JSONObject();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String key = entry.getKey();
            if (SELECTED_KEY.equals(key) || key.endsWith("Name") || key.endsWith("Desc")
                    || "createTime".equals(key) || "updateTime".equals(key)) {
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
                messageLabel.setText(wrapper.optString("message", "読み込みに失敗しました。"));
                return;
            }

            JSONObject data = wrapper.optJSONObject("data");
            if (data == null) {
                messageLabel.setText("レスポンスのデータが空です。");
                return;
            }

            long total = data.optLong("total", 0);
            long totalPages = data.optLong("totalPages", 0);
            pageInfoLabel.setText(String.format("%d / %d ページ, 合計 %d 件", pageNum, totalPages, total));

            JSONArray records = data.optJSONArray("records");
            buildTable(records == null ? new JSONArray() : records);
            messageLabel.setText("読み込み成功");

            if (totalPages > 0 && pageNum > totalPages) {
                pageNum = (int) totalPages;
                loadData();
            }
        } catch (Exception ex) {
            messageLabel.setText("読み込みに失敗しました: " + ex.getMessage());
        }
    }

    private void buildTable(JSONArray records) {
        dataTable.getColumns().clear();

        ObservableList<Map<String, Object>> rows = FXCollections.observableArrayList();
        LinkedHashSet<String> keys = new LinkedHashSet<>();

        for (int i = 0; i < records.length(); i++) {
            JSONObject item = records.getJSONObject(i);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(SELECTED_KEY, new SimpleBooleanProperty(false));
            for (String key : item.keySet()) {
                Object value = item.opt(key);
                row.put(key, value);
                keys.add(key);
            }
            rows.add(row);
        }

        dataTable.getColumns().add(createSelectColumn(rows));
        for (String key : orderedKeys(keys)) {
            dataTable.getColumns().add(createTextColumn(key));
        }
        if ("stockOrder".equals(currentModule)) {
            dataTable.getColumns().add(createDetailActionColumn());
        }

        dataTable.setItems(rows);
    }

    private TableColumn<Map<String, Object>, Void> createDetailActionColumn() {
        TableColumn<Map<String, Object>, Void> col = new TableColumn<>("明細");
        col.setPrefWidth(90);
        col.setSortable(false);
        col.setReorderable(false);
        col.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("明細");
            {
                btn.setOnAction(event -> {
                    Map<String, Object> row = getTableView().getItems().get(getIndex());
                    Object orderId = row.get("id");
                    if (orderId == null) {
                        messageLabel.setText("伝票IDが取得できません");
                        return;
                    }
                    switchModule("stockOrderItem", "入出庫明細");
                    Control control = queryControls.get("orderId");
                    if (control instanceof TextField tf) {
                        tf.setText(String.valueOf(orderId));
                    }
                    pageNum = 1;
                    loadData();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        return col;
    }

    private TableColumn<Map<String, Object>, Boolean> createSelectColumn(ObservableList<Map<String, Object>> rows) {
        TableColumn<Map<String, Object>, Boolean> column = new TableColumn<>();
        CheckBox selectAll = new CheckBox();
        selectAll.setOnAction(event -> rows.forEach(row -> selectedProperty(row).set(selectAll.isSelected())));
        column.setGraphic(selectAll);
        column.setEditable(true);
        column.setSortable(false);
        column.setReorderable(false);
        column.setPrefWidth(46);
        column.setMinWidth(46);
        column.setMaxWidth(46);
        column.setCellValueFactory(cell -> selectedProperty(cell.getValue()));
        column.setCellFactory(CheckBoxTableCell.forTableColumn(column));
        column.getStyleClass().add("select-column");
        return column;
    }

    private TableColumn<Map<String, Object>, String> createTextColumn(String key) {
        TableColumn<Map<String, Object>, String> col = new TableColumn<>(columnTitle(key));
        col.setPrefWidth("id".equals(key) || "skuId".equals(key) ? 90 : 150);
        col.setEditable(true);
        col.setCellFactory(TextFieldTableCell.forTableColumn());
        col.setCellValueFactory(cell -> {
            Object v = cell.getValue().getOrDefault(key, "");
            if ("status".equals(key)) {
                String s = String.valueOf(v);
                if ("1".equals(s) || "ENABLE".equalsIgnoreCase(s)) {
                    return new SimpleStringProperty("有効");
                }
                if ("0".equals(s) || "DISABLE".equalsIgnoreCase(s)) {
                    return new SimpleStringProperty("無効");
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
                if ("有効".equals(val) || "1".equals(val) || "ENABLE".equalsIgnoreCase(val)) {
                    row.put(key, "ENABLE");
                } else if ("無効".equals(val) || "0".equals(val) || "DISABLE".equalsIgnoreCase(val)) {
                    row.put(key, "DISABLE");
                } else {
                    row.put(key, val);
                }
            } else {
                row.put(key, val);
            }
            dataTable.refresh();
        });
        return col;
    }

    private List<String> orderedKeys(LinkedHashSet<String> keys) {
        List<String> ordered = new ArrayList<>();
        if (keys.contains("id")) {
            ordered.add("id");
        }
        if (keys.contains("skuId")) {
            ordered.add("skuId");
        }
        for (String key : keys) {
            if (!"id".equals(key) && !"skuId".equals(key) && !SELECTED_KEY.equals(key)) {
                ordered.add(key);
            }
        }
        return ordered;
    }

    private String columnTitle(String key) {
        if ("id".equals(key) || "skuId".equals(key)) {
            return "ID";
        }
        return ModuleMeta.normalizeTitle(key);
    }

    private SimpleBooleanProperty selectedProperty(Map<String, Object> row) {
        Object selected = row.get(SELECTED_KEY);
        if (selected instanceof SimpleBooleanProperty property) {
            return property;
        }
        SimpleBooleanProperty property = new SimpleBooleanProperty(false);
        row.put(SELECTED_KEY, property);
        return property;
    }

    private List<Map<String, Object>> checkedRows() {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (dataTable.getItems() == null) {
            return rows;
        }
        for (Map<String, Object> row : dataTable.getItems()) {
            if (selectedProperty(row).get()) {
                rows.add(row);
            }
        }
        return rows;
    }

    private Map<String, Object> currentWorkingRow() {
        Map<String, Object> selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            return selected;
        }
        List<Map<String, Object>> checkedRows = checkedRows();
        return checkedRows.isEmpty() ? null : checkedRows.get(0);
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
}
