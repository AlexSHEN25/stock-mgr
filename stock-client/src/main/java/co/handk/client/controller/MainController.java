package co.handk.client.controller;

import co.handk.client.MainApp;
import co.handk.client.constant.UiText;
import co.handk.client.model.Session;
import co.handk.client.service.ModuleDataService;
import co.handk.client.service.TableActionService;
import co.handk.client.service.UiFeedbackService;
import co.handk.client.util.ApiClient;
import co.handk.client.util.ModuleMeta;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static co.handk.client.constant.AppConstants.ApiPath;
import static co.handk.client.constant.AppConstants.Field;
import static co.handk.client.constant.AppConstants.Module;

public class MainController {

    private static final String SELECTED_KEY = "__selected";
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    @FXML private Label currentUserLabel;
    @FXML private Label pageTitleLabel;
    @FXML private Label pageInfoLabel;
    @FXML private Label messageLabel;
    @FXML private TextField deleteIdField;
    @FXML private TableView<Map<String, Object>> dataTable;
    @FXML private FlowPane queryFieldsPane;

    private MainApp app;
    private String currentModule = Module.USER;
    private int pageNum = 1;
    private final int pageSize = 10;
    private final Map<String, Control> queryControls = new LinkedHashMap<>();
    private final ModuleDataService dataService = new ModuleDataService();
    private final TableActionService tableActionService = new TableActionService();
    private final UiFeedbackService uiFeedback = new UiFeedbackService();
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
        messageLabel.setText("左メニューからモジュールを選択し、検索後に操作してください。");
        Platform.runLater(this::focusFirstQueryField);
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
            messageLabel.setText(UiText.MSG_SELECT_ROW_FIRST);
            return;
        }
        inlineEditingRow = selected;
        inlineBackup = new LinkedHashMap<>(selected);
        inlineBackup.remove(SELECTED_KEY);
        messageLabel.setText(UiText.MSG_INLINE_EDIT_STARTED);
    }

    @FXML
    private void onInlineSave() {
        if (inlineEditingRow == null) {
            messageLabel.setText(UiText.MSG_INLINE_EDIT_NONE);
            return;
        }
        try {
            JSONObject dto = normalizeRowForUpdate(inlineEditingRow);
            JSONObject json = dataService.save(currentModule, true, dto);
            if (uiFeedback.isSuccess(json)) {
                messageLabel.setText(UiText.MSG_INLINE_UPDATE_SUCCESS);
                inlineEditingRow = null;
                inlineBackup = null;
                loadData();
            } else {
                messageLabel.setText(uiFeedback.resolveMessage(json, UiText.MSG_INLINE_UPDATE_FAILED));
            }
        } catch (Exception ex) {
            messageLabel.setText(UiText.MSG_UPDATE_FAILED + ex.getMessage());
        }
    }

    @FXML
    private void onInlineCancel() {
        if (inlineEditingRow == null || inlineBackup == null) {
            messageLabel.setText(UiText.MSG_INLINE_EDIT_NONE);
            return;
        }
        SimpleBooleanProperty selectedProperty = selectedProperty(inlineEditingRow);
        inlineEditingRow.clear();
        inlineEditingRow.putAll(inlineBackup);
        inlineEditingRow.put(SELECTED_KEY, selectedProperty);
        inlineEditingRow = null;
        inlineBackup = null;
        dataTable.refresh();
        messageLabel.setText(UiText.MSG_INLINE_CANCELLED);
    }

    @FXML
    private void onEdit() {
        if (currentWorkingRow() == null) {
            messageLabel.setText(UiText.MSG_SELECT_ROW_FIRST);
            return;
        }
        openFormDialog("編集", true);
    }

    @FXML
    private void onBatchDelete() {
        List<Map<String, Object>> checkedRows = checkedRows();
        if (checkedRows.isEmpty()) {
            messageLabel.setText(UiText.MSG_BATCH_DELETE_CHECK);
            return;
        }

        List<String> ids = new ArrayList<>();
        for (Map<String, Object> row : checkedRows) {
            ids.add(resolveRecordId(row));
        }
        if (!confirm("一括削除確認", ids.size() + "件を削除します。よろしいですか？")) {
            messageLabel.setText("一括削除をキャンセルしました。");
            return;
        }
        int ok = tableActionService.batchDelete(currentModule, ids);
        messageLabel.setText(String.format(UiText.MSG_BATCH_DELETE_DONE, ok));
        loadData();
    }

    @FXML
    private void onDelete() {
        String id = deleteIdField.getText();
        if (id == null || id.isBlank()) {
            messageLabel.setText(UiText.MSG_DELETE_ID_REQUIRED);
            return;
        }

        try {
            if (!confirm("削除確認", "ID " + id.trim() + " を削除します。よろしいですか？")) {
                messageLabel.setText("削除をキャンセルしました。");
                return;
            }
            JSONObject json = tableActionService.deleteOne(currentModule, id.trim());
            if (uiFeedback.isSuccess(json)) {
                messageLabel.setText(UiText.MSG_DELETE_SUCCESS);
                loadData();
            } else {
                messageLabel.setText(uiFeedback.resolveMessage(json, UiText.MSG_DELETE_FAILED));
            }
        } catch (Exception ex) {
            messageLabel.setText(uiFeedback.deleteFailed(ex));
        }
    }

    @FXML
    private void onLogout() {
        if (!confirm("ログアウト確認", "ログアウトします。よろしいですか？")) {
            return;
        }
        try {
            String res = ApiClient.post(ApiPath.USER_LOGOUT, "{}");
            JSONObject json = new JSONObject(res);
            if (uiFeedback.isSuccess(json)) {
                Session.clear();
                app.showLogin();
            } else {
                messageLabel.setText(uiFeedback.resolveMessage(json, UiText.MSG_LOGOUT_FAILED));
            }
        } catch (Exception ex) {
            messageLabel.setText(uiFeedback.logoutFailed(ex));
        }
    }

    private String resolveRecordId(Map<String, Object> row) {
        Object id = row.get(Field.ID);
        if (id == null) {
            id = row.get(Field.SKU_ID);
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
        Platform.runLater(this::focusFirstQueryField);
    }

    private Control createControl(String field) {
        ModuleMeta.FieldType type = ModuleMeta.fieldType(currentModule, field);
        if (type == ModuleMeta.FieldType.SELECT) {
            ComboBox<Option> combo = new ComboBox<>();
            List<ModuleMeta.Option> options = ModuleMeta.selectOptions(currentModule, field);
            for (ModuleMeta.Option option : options) {
                combo.getItems().add(new Option(option.label, option.value));
            }
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
            JSONObject wrapper = dataService.fetchPage(module, 1, 50, Map.of());
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
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Load relation options failed. module=" + module, ex);
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
            messageLabel.setText(uiFeedback.formOpenFailed(ex));
        }
    }

    private void submitForm(boolean editMode, JSONObject dto) {
        try {
            JSONObject json = dataService.save(currentModule, editMode, dto);
            if (uiFeedback.isSuccess(json)) {
                messageLabel.setText(uiFeedback.saveSuccess(editMode));
                loadData();
            } else {
                messageLabel.setText(uiFeedback.resolveMessage(json, UiText.MSG_SAVE_FAILED));
            }
        } catch (Exception ex) {
            messageLabel.setText(uiFeedback.saveFailed(ex));
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

    private void loadData() {
        try {
            JSONObject wrapper = dataService.fetchPage(currentModule, pageNum, pageSize, buildQueryParams());
            if (!uiFeedback.isSuccess(wrapper)) {
                messageLabel.setText(uiFeedback.resolveMessage(wrapper, UiText.MSG_LOAD_FAILED));
                return;
            }

            JSONObject data = wrapper.optJSONObject("data");
            if (data == null) {
                messageLabel.setText(UiText.MSG_RESPONSE_DATA_EMPTY);
                return;
            }

            long total = data.optLong("total", 0);
            long totalPages = data.optLong("totalPages", 0);
            pageInfoLabel.setText(String.format("%d / %d ページ, 全 %d 件", pageNum, totalPages, total));

            JSONArray records = data.optJSONArray("records");
            buildTable(records == null ? new JSONArray() : records);
            if (records == null || records.isEmpty()) {
                messageLabel.setText("該当データがありません。条件を変更して再検索してください。");
            } else {
                messageLabel.setText(UiText.MSG_LOAD_SUCCESS);
            }

            if (totalPages > 0 && pageNum > totalPages) {
                pageNum = (int) totalPages;
                loadData();
            }
        } catch (Exception ex) {
            messageLabel.setText(uiFeedback.loadFailed(ex));
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
        for (String key : ModuleMeta.orderedColumns(currentModule, keys)) {
            if (!keys.contains(key)) {
                continue;
            }
            dataTable.getColumns().add(createTextColumn(key));
        }
        if (Module.STOCK_ORDER.equals(currentModule)) {
            dataTable.getColumns().add(createDetailActionColumn("入出庫明細", Module.STOCK_ORDER_ITEM, Field.ORDER_ID));
        }
        if (Module.REQUEST_FORM.equals(currentModule)) {
            dataTable.getColumns().add(createDetailActionColumn("申請明細", Module.REQUEST_ITEM, Field.REQUEST_ID));
            dataTable.getColumns().add(createDownloadActionColumn());
        }
        dataTable.setItems(rows);
    }

    private TableColumn<Map<String, Object>, Void> createDownloadActionColumn() {
        TableColumn<Map<String, Object>, Void> col = new TableColumn<>("ダウンロード");
        col.setPrefWidth(120);
        col.setSortable(false);
        col.setReorderable(false);
        col.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("ダウンロード");
            {
                btn.setOnAction(event -> {
                    Map<String, Object> row = getTableView().getItems().get(getIndex());
                    Object id = row.get(Field.ID);
                    if (id == null) {
                        messageLabel.setText(UiText.MSG_RELATION_ID_NOT_FOUND);
                        return;
                    }
                    downloadRequestForm(String.valueOf(id));
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

    private void downloadRequestForm(String id) {
        try {
            byte[] bytes = tableActionService.downloadRequestForm(id);

            FileChooser chooser = new FileChooser();
            chooser.setTitle("保存先を選択");
            chooser.setInitialFileName("request_" + id + ".xlsx");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
            Window owner = dataTable != null && dataTable.getScene() != null ? dataTable.getScene().getWindow() : null;
            File target = chooser.showSaveDialog(owner);
            if (target == null) {
                return;
            }
            try (FileOutputStream fos = new FileOutputStream(target)) {
                fos.write(bytes);
            }
            messageLabel.setText(UiText.MSG_DOWNLOAD_DONE);
        } catch (Exception ex) {
            messageLabel.setText(uiFeedback.downloadFailed(ex));
        }
    }

    private TableColumn<Map<String, Object>, Void> createDetailActionColumn(String title, String targetModule, String filterField) {
        TableColumn<Map<String, Object>, Void> col = new TableColumn<>(title);
        col.setPrefWidth(100);
        col.setSortable(false);
        col.setReorderable(false);
        col.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button(title);
            {
                btn.setOnAction(event -> {
                    Map<String, Object> row = getTableView().getItems().get(getIndex());
                    Object id = row.get(Field.ID);
                    if (id == null) {
                        messageLabel.setText(UiText.MSG_RELATION_ID_NOT_FOUND);
                        return;
                    }
                    switchModule(targetModule, title);
                    Control control = queryControls.get(filterField);
                    if (control instanceof TextField tf) {
                        tf.setText(String.valueOf(id));
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
            if (Field.STATUS.equals(key)) {
                String normalized = normalizeEnumValue(String.valueOf(v));
                ModuleMeta.Option opt = ModuleMeta.optionByValue(currentModule, key, normalized);
                if (opt != null) {
                    return new SimpleStringProperty(opt.label);
                }
            }
            ModuleMeta.Option option = ModuleMeta.optionByValue(currentModule, key, String.valueOf(v));
            if (option != null) {
                return new SimpleStringProperty(option.label);
            }
            return new SimpleStringProperty(String.valueOf(v));
        });
        col.setOnEditCommit(evt -> {
            Map<String, Object> row = evt.getRowValue();
            if (row == null) {
                return;
            }
            String val = evt.getNewValue();
            if (ModuleMeta.fieldType(currentModule, key) == ModuleMeta.FieldType.SELECT) {
                ModuleMeta.Option byLabel = ModuleMeta.optionByLabel(currentModule, key, val);
                row.put(key, byLabel != null ? byLabel.value : normalizeEnumValue(val));
            } else {
                row.put(key, val);
            }
            dataTable.refresh();
        });
        return col;
    }

    private String normalizeEnumValue(String value) {
        if (value == null) {
            return "";
        }
        if ("ENABLE".equalsIgnoreCase(value)) {
            return "1";
        }
        if ("DISABLE".equalsIgnoreCase(value)) {
            return "0";
        }
        return value;
    }

    private String columnTitle(String key) {
        if (Field.ID.equals(key) || Field.SKU_ID.equals(key)) {
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

    private void focusFirstQueryField() {
        for (Control control : queryControls.values()) {
            if (!control.isDisabled() && control.isVisible()) {
                control.requestFocus();
                return;
            }
        }
    }

    private boolean confirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
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
