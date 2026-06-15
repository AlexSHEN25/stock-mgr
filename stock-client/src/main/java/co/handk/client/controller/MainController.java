package co.handk.client.controller;

import co.handk.client.MainApp;
import co.handk.client.constant.UiText;
import co.handk.client.model.Session;
import co.handk.client.service.ModuleDataService;
import co.handk.client.service.TableActionService;
import co.handk.client.service.TableRowService;
import co.handk.client.service.UiFeedbackService;
import co.handk.client.service.UserAccountService;
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
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Label;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static co.handk.client.constant.AppConstants.Field;
import static co.handk.client.constant.AppConstants.Module;

public class MainController {

    private static final String SELECTED_KEY = "__selected";
    private static final String STOCK_TAB_SELECTED_STYLE = "tab-btn-selected";
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    @FXML private Label currentUserLabel;
    @FXML private Label pageTitleLabel;
    @FXML private Label pageInfoLabel;
    @FXML private Label messageLabel;
    @FXML private TextField deleteIdField;
    @FXML private Button stockInboundButton;
    @FXML private Button stockOutboundButton;
    @FXML private Button addButton;
    @FXML private Button readAllButton;
    @FXML private Button inlineEditButton;
    @FXML private Button inlineSaveButton;
    @FXML private Button inlineCancelButton;
    @FXML private Button editButton;
    @FXML private Button batchDeleteButton;
    @FXML private Button deleteButton;
    @FXML private HBox stockSubNav;
    @FXML private Button stockOrderTabButton;
    @FXML private Button stockOrderItemTabButton;
    @FXML private TableView<Map<String, Object>> dataTable;
    @FXML private FlowPane queryFieldsPane;

    private MainApp app;
    private String currentModule = Module.USER;
    private int pageNum = 1;
    private static final int PAGE_SIZE = 10;
    private final Map<String, Control> queryControls = new LinkedHashMap<>();
    private final ModuleDataService dataService = new ModuleDataService();
    private final TableActionService tableActionService = new TableActionService();
    private final TableRowService tableRowService = new TableRowService();
    private final UiFeedbackService uiFeedback = new UiFeedbackService();
    private final UserAccountService userAccountService = new UserAccountService();
    private final Map<String, Integer> pageNumByModule = new HashMap<>();
    private Map<String, Object> inlineEditingRow;
    private Map<String, Object> inlineBackup;
    private String pendingStockOperation;

    public void setApp(MainApp app) {
        this.app = app;
        currentUserLabel.setText(UiText.LABEL_LOGIN_USER_PREFIX + Session.getUsername());
        dataTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        dataTable.getSelectionModel().setCellSelectionEnabled(true);
        dataTable.setEditable(true);
        dataTable.setTableMenuButtonVisible(true);
        dataTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        rebuildQueryFields();
        applyActionPolicy();
        messageLabel.setText(UiText.MSG_FIRST_GUIDE);
        Platform.runLater(this::focusFirstQueryField);
        loadData();
    }

    @FXML
    private void onNavSelect(ActionEvent event) {
        if (event.getSource() instanceof Button btn) {
            switchModule(String.valueOf(btn.getUserData()), btn.getText());
        }
    }

    @FXML
    private void onRefresh() {
        resetQueryControls();
        pageNum = 1;
        pageNumByModule.put(currentModule, pageNum);
        loadData();
    }

    @FXML
    private void onSearch() {
        pageNum = 1;
        pageNumByModule.put(currentModule, pageNum);
        loadData();
    }

    @FXML
    private void onPrevPage() {
        if (pageNum > 1) {
            pageNum--;
            pageNumByModule.put(currentModule, pageNum);
            loadData();
        }
    }

    @FXML
    private void onNextPage() {
        pageNum++;
        pageNumByModule.put(currentModule, pageNum);
        loadData();
    }

    @FXML
    private void onAdd() {
        if (isReadOnlyModule()) {
            messageLabel.setText(UiText.MSG_READONLY_MODULE);
            return;
        }
        pendingStockOperation = null;
        openFormDialog(UiText.ACTION_CREATE, false);
    }

    @FXML
    private void onStockInbound() {
        if (!isStockOperationModule()) {
            return;
        }
        Map<String, Object> selected = currentWorkingRow();
        if (selected == null) {
            messageLabel.setText(UiText.MSG_SELECT_ROW_FIRST);
            return;
        }
        pendingStockOperation = "inbound";
        openFormDialog(UiText.byKey("main.btn.stockInbound"), false, Module.STOCK, selected);
    }

    @FXML
    private void onStockOutbound() {
        if (!isStockOperationModule()) {
            return;
        }
        Map<String, Object> selected = currentWorkingRow();
        if (selected == null) {
            messageLabel.setText(UiText.MSG_SELECT_ROW_FIRST);
            return;
        }
        pendingStockOperation = "outbound";
        openFormDialog(UiText.byKey("main.btn.stockOutbound"), false, Module.STOCK, selected);
    }

    @FXML
    private void onInlineEdit() {
        if (isReadOnlyModule()) {
            messageLabel.setText(UiText.MSG_READONLY_MODULE);
            return;
        }
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
    private void onReadAll() {
        if (!Module.MESSAGE.equals(currentModule)) {
            return;
        }
        try {
            JSONObject json = tableActionService.readAllMessages();
            if (uiFeedback.isSuccess(json)) {
                messageLabel.setText(UiText.MSG_READ_ALL_DONE);
                loadData();
            } else {
                messageLabel.setText(uiFeedback.resolveMessage(json, UiText.MSG_READ_ALL_FAIL));
            }
        } catch (Exception ex) {
            messageLabel.setText(UiText.MSG_READ_ALL_FAIL + ": " + ex.getMessage());
        }
    }

    @FXML
    private void onInlineSave() {
        if (isReadOnlyModule()) {
            messageLabel.setText(UiText.MSG_READONLY_MODULE);
            return;
        }
        if (inlineEditingRow == null) {
            messageLabel.setText(UiText.MSG_INLINE_EDIT_NONE);
            return;
        }
        try {
            JSONObject dto = tableRowService.normalizeForUpdate(currentModule, inlineEditingRow, SELECTED_KEY);
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
        SimpleBooleanProperty selectedProperty = tableRowService.selectedProperty(inlineEditingRow, SELECTED_KEY);
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
        if (isReadOnlyModule()) {
            messageLabel.setText(UiText.MSG_READONLY_MODULE);
            return;
        }
        if (currentWorkingRow() == null) {
            messageLabel.setText(UiText.MSG_SELECT_ROW_FIRST);
            return;
        }
        openFormDialog(UiText.ACTION_EDIT, true);
    }

    @FXML
    private void onBatchDelete() {
        if (isReadOnlyModule()) {
            messageLabel.setText(UiText.MSG_READONLY_MODULE);
            return;
        }
        List<Map<String, Object>> checkedRows = checkedRows();
        if (checkedRows.isEmpty()) {
            messageLabel.setText(UiText.MSG_BATCH_DELETE_CHECK);
            return;
        }
        List<String> ids = tableRowService.resolveRecordIds(checkedRows);
        if (!confirm(UiText.TITLE_CONFIRM_BATCH_DELETE, String.format(UiText.MSG_CONFIRM_BATCH_DELETE, ids.size()))) {
            messageLabel.setText(UiText.MSG_BATCH_DELETE_CANCELLED);
            return;
        }
        int ok = tableActionService.batchDelete(currentModule, ids);
        messageLabel.setText(String.format(UiText.MSG_BATCH_DELETE_DONE, ok));
        loadData();
    }

    @FXML
    private void onDelete() {
        if (isReadOnlyModule()) {
            messageLabel.setText(UiText.MSG_READONLY_MODULE);
            return;
        }
        String id = deleteIdField.getText();
        if (id == null || id.isBlank()) {
            messageLabel.setText(UiText.MSG_DELETE_ID_REQUIRED);
            return;
        }
        String normalizedId = id.trim();
        if (!confirm(UiText.TITLE_CONFIRM_DELETE, String.format(UiText.MSG_CONFIRM_DELETE, normalizedId))) {
            messageLabel.setText(UiText.MSG_DELETE_CANCELLED);
            return;
        }
        try {
            JSONObject json = tableActionService.deleteOne(currentModule, normalizedId);
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
    private void onChangePassword() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(UiText.TITLE_CHANGE_PASSWORD);
        dialog.setHeaderText(null);
        Window owner = dataTable != null && dataTable.getScene() != null ? dataTable.getScene().getWindow() : null;
        if (owner != null) {
            dialog.initOwner(owner);
        }

        PasswordField passwordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();
        passwordField.setPromptText(UiText.FIELD_NEW_PASSWORD);
        confirmPasswordField.setPromptText(UiText.FIELD_CONFIRM_PASSWORD);

        GridPane content = new GridPane();
        content.setHgap(10);
        content.setVgap(10);
        content.add(new Label(UiText.FIELD_NEW_PASSWORD), 0, 0);
        content.add(passwordField, 1, 0);
        content.add(new Label(UiText.FIELD_CONFIRM_PASSWORD), 0, 1);
        content.add(confirmPasswordField, 1, 1);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        String password = passwordField.getText();
        if (password == null || password.isBlank()) {
            messageLabel.setText(UiText.MSG_NEW_PASSWORD_REQUIRED);
            return;
        }
        if (!password.equals(confirmPasswordField.getText())) {
            messageLabel.setText(UiText.MSG_PASSWORD_MISMATCH);
            return;
        }
        try {
            JSONObject json = userAccountService.changePassword(Session.getUserId(), password);
            messageLabel.setText(uiFeedback.isSuccess(json)
                    ? UiText.MSG_PASSWORD_CHANGE_SUCCESS
                    : uiFeedback.resolveMessage(json, UiText.MSG_PASSWORD_CHANGE_FAILED));
        } catch (Exception ex) {
            messageLabel.setText(UiText.MSG_PASSWORD_CHANGE_FAILED + ": " + ex.getMessage());
        }
    }

    @FXML
    private void onLogout() {
        if (!confirm(UiText.TITLE_CONFIRM_LOGOUT, UiText.MSG_CONFIRM_LOGOUT)) {
            return;
        }
        try {
            JSONObject json = userAccountService.logout();
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

    private void switchModule(String module, String title) {
        pageNumByModule.put(currentModule, pageNum);
        currentModule = module;
        pageNum = pageNumByModule.getOrDefault(module, 1);
        pageTitleLabel.setText(title);
        inlineEditingRow = null;
        inlineBackup = null;
        updateStockSubNav();
        rebuildQueryFields();
        applyActionPolicy();
        loadData();
    }

    @FXML
    private void onStockOrderTab() {
        switchModule(Module.STOCK_ORDER, UiText.MENU_STOCK_ORDER);
    }

    @FXML
    private void onStockOrderItemTab() {
        switchModule(Module.STOCK_ORDER_ITEM, UiText.MENU_STOCK_ORDER_ITEM);
    }

    private void rebuildQueryFields() {
        queryFieldsPane.getChildren().clear();
        queryControls.clear();
        for (String field : ModuleMeta.visibleQueryFields(currentModule)) {
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

    private void applyActionPolicy() {
        ModuleMeta.ModuleActionPolicy policy = ModuleMeta.actionPolicy(currentModule);
        boolean canWrite = ModuleMeta.canWriteByPermission(currentModule);
        boolean messageModule = Module.MESSAGE.equals(currentModule);
        boolean stockModule = isStockOperationModule();
        boolean stockOperationAllowed = ModuleMeta.canWriteByPermission(Module.STOCK);
        updateStockSubNav();
        addButton.setDisable(!policy.canCreate || !canWrite);
        stockInboundButton.setVisible(stockModule);
        stockInboundButton.setManaged(stockModule);
        stockOutboundButton.setVisible(stockModule);
        stockOutboundButton.setManaged(stockModule);
        stockInboundButton.setDisable(!stockOperationAllowed);
        stockOutboundButton.setDisable(!stockOperationAllowed);
        readAllButton.setVisible(messageModule);
        readAllButton.setManaged(messageModule);
        readAllButton.setDisable(!messageModule || !canWrite);
        inlineEditButton.setDisable(!policy.canInlineEdit || !canWrite);
        inlineSaveButton.setDisable(!policy.canInlineEdit || !canWrite);
        inlineCancelButton.setDisable(!policy.canInlineEdit || !canWrite);
        editButton.setDisable(!policy.canEdit || !canWrite);
        batchDeleteButton.setDisable(!policy.canBatchDelete || !canWrite);
        deleteIdField.setDisable(!policy.canDelete || !canWrite);
        deleteButton.setDisable(!policy.canDelete || !canWrite);
    }

    private void updateStockSubNav() {
        boolean stockModule = Module.STOCK_ORDER.equals(currentModule) || Module.STOCK_ORDER_ITEM.equals(currentModule);
        stockSubNav.setVisible(stockModule);
        stockSubNav.setManaged(stockModule);
        if (!stockModule) {
            stockOrderTabButton.getStyleClass().remove(STOCK_TAB_SELECTED_STYLE);
            stockOrderItemTabButton.getStyleClass().remove(STOCK_TAB_SELECTED_STYLE);
            return;
        }
        boolean orderView = Module.STOCK_ORDER.equals(currentModule);
        stockOrderTabButton.setDisable(orderView);
        stockOrderItemTabButton.setDisable(!orderView);
        if (orderView) {
            addSelectedStyle(stockOrderTabButton);
            removeSelectedStyle(stockOrderItemTabButton);
        } else {
            addSelectedStyle(stockOrderItemTabButton);
            removeSelectedStyle(stockOrderTabButton);
        }
    }

    private void addSelectedStyle(Button button) {
        if (button != null && !button.getStyleClass().contains(STOCK_TAB_SELECTED_STYLE)) {
            button.getStyleClass().add(STOCK_TAB_SELECTED_STYLE);
        }
    }

    private void removeSelectedStyle(Button button) {
        if (button != null) {
            button.getStyleClass().remove(STOCK_TAB_SELECTED_STYLE);
        }
    }

    private Control createControl(String field) {
        String displayField = field;
        ModuleMeta.FieldType type = ModuleMeta.fieldType(currentModule, field);
        if (type == ModuleMeta.FieldType.SELECT) {
            ComboBox<Option> combo = new ComboBox<>();
            for (ModuleMeta.Option option : ModuleMeta.selectOptions(currentModule, field)) {
                combo.getItems().add(new Option(option.label, option.value));
            }
            combo.setPromptText(queryPlaceholder(displayField, true));
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
            combo.setPromptText(queryPlaceholder(displayField, true));
            return combo;
        }
        TextField textField = new TextField();
        textField.setPromptText(queryPlaceholder(displayField, false));
        return textField;
    }

    private String queryPlaceholder(String field, boolean selectMode) {
        if ("deptName".equals(field)) {
            return UiText.MSG_SELECT_DEPT;
        }
        return ModuleMeta.normalizeTitle(field) + (selectMode ? UiText.MSG_SELECT_SUFFIX : UiText.MSG_SEARCH_SUFFIX);
    }

    private List<Option> fetchRelationOptions(String module) {
        List<Option> options = new ArrayList<>();
        try {
            for (Map<String, String> item : dataService.fetchSimpleRelationOptions(module, Map.of())) {
                options.add(new Option(item.get("label"), item.get("value")));
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
            String value = readControlValue(entry.getValue());
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
        openFormDialog(action, editMode, currentModule, editMode ? currentWorkingRow() : null);
    }

    private void openFormDialog(String action, boolean editMode, String formModule, Map<String, Object> source) {
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
            formController.configure(
                    formModule,
                    action + " " + pageTitleLabel.getText(),
                    editMode,
                    source,
                    tableRowService.visibleKeys(dataTable.getItems(), SELECTED_KEY));
            modal.setOnCloseRequest(e -> formController.markCanceled());
            modal.showAndWait();
            if (formController.isSubmitted()) {
                submitForm(editMode, formController.toJson());
            }
        } catch (Exception ex) {
            messageLabel.setText(uiFeedback.formOpenFailed(ex));
        }
    }

    private void submitForm(boolean editMode, JSONObject dto) {
        try {
            String formModule = pendingStockOperation == null ? currentModule : Module.STOCK;
            dto = ModuleMeta.applyFormValueRules(formModule, dto);
            if (pendingStockOperation != null) {
                dto = sanitizeStockOperationPayload(dto);
            }
            JSONObject json;
            if ("outbound".equals(pendingStockOperation)) {
                json = dataService.outboundStock(dto);
            } else if ("inbound".equals(pendingStockOperation)) {
                json = dataService.inboundStock(dto);
            } else {
                json = dataService.save(currentModule, editMode, dto);
            }
            if (uiFeedback.isSuccess(json)) {
                messageLabel.setText(uiFeedback.saveSuccess(editMode));
                loadData();
            } else {
                messageLabel.setText(uiFeedback.resolveMessage(json, UiText.MSG_SAVE_FAILED));
            }
        } catch (Exception ex) {
            messageLabel.setText(uiFeedback.saveFailed(ex));
        } finally {
            pendingStockOperation = null;
        }
    }

    private void loadData() {
        try {
            JSONObject wrapper = dataService.fetchPage(currentModule, pageNum, PAGE_SIZE, buildQueryParams());
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
            pageInfoLabel.setText(String.format(UiText.PAGE_INFO_FORMAT, pageNum, totalPages, total));

            JSONArray records = data.optJSONArray("records");
            buildTable(records == null ? new JSONArray() : records);
            messageLabel.setText(records == null || records.isEmpty() ? UiText.MSG_EMPTY_RESULT : UiText.MSG_LOAD_SUCCESS);

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
        ObservableList<Map<String, Object>> rows = FXCollections.observableArrayList(
                tableRowService.createRows(records, SELECTED_KEY));
        LinkedHashSet<String> keys = tableRowService.visibleKeys(rows, SELECTED_KEY);

        dataTable.getColumns().add(createSelectColumn(rows));
        for (String key : ModuleMeta.orderedColumns(currentModule, keys)) {
            if (keys.contains(key)) {
                dataTable.getColumns().add(createTextColumn(key));
            }
        }
        for (ModuleMeta.RowAction action : ModuleMeta.rowActions(currentModule)) {
            dataTable.getColumns().add(createActionColumn(action));
        }
        dataTable.setItems(rows);
    }

    private TableColumn<Map<String, Object>, Void> createActionColumn(ModuleMeta.RowAction action) {
        if (action.type == ModuleMeta.RowActionType.DOWNLOAD_REQUEST_FORM) {
            return createDownloadActionColumn();
        }
        if (action.type == ModuleMeta.RowActionType.MATCH_REQUEST_ITEMS) {
            return createMatchRequestItemsActionColumn(UiText.byKey(action.titleKey));
        }
        if (action.type == ModuleMeta.RowActionType.PREVIEW_FIELDS) {
            return createPreviewFieldsActionColumn(UiText.byKey(action.titleKey), action.detailFields);
        }
        if (action.type == ModuleMeta.RowActionType.MARK_READ) {
            return createMarkReadActionColumn(UiText.byKey(action.titleKey));
        }
        if (action.type == ModuleMeta.RowActionType.APPROVE_ORDER) {
            return createApproveOrderActionColumn(UiText.ACTION_APPROVE_ORDER, true);
        }
        if (action.type == ModuleMeta.RowActionType.REJECT_ORDER) {
            return createApproveOrderActionColumn(UiText.ACTION_REJECT_ORDER, false);
        }
        return createDetailActionColumn(UiText.byKey(action.titleKey), action.targetModule, action.filterField);
    }

    private TableColumn<Map<String, Object>, Void> createApproveOrderActionColumn(String title, boolean approved) {
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
                    String remark = null;
                    if (!approved) {
                        TextInputDialog dialog = new TextInputDialog();
                        dialog.setTitle(title);
                        dialog.setHeaderText(title);
                        dialog.setContentText("备注");
                        Window owner = dataTable != null && dataTable.getScene() != null ? dataTable.getScene().getWindow() : null;
                        if (owner != null) {
                            dialog.initOwner(owner);
                        }
                        Optional<String> result = dialog.showAndWait();
                        if (result.isEmpty()) {
                            return;
                        }
                        remark = result.get().trim();
                    }
                    try {
                        JSONObject json = tableActionService.approveStockOrder(String.valueOf(id), approved, remark);
                        if (uiFeedback.isSuccess(json)) {
                            messageLabel.setText(approved ? "审批已通过" : "审批已拒绝");
                            loadData();
                        } else {
                            messageLabel.setText(uiFeedback.resolveMessage(json, UiText.MSG_SAVE_FAILED));
                        }
                    } catch (Exception ex) {
                        messageLabel.setText((approved ? "审批通过失败" : "审批拒绝失败") + ": " + ex.getMessage());
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Map<String, Object> row = getTableView().getItems().get(getIndex());
                boolean approving = "1".equals(String.valueOf(row.get(Field.STATE)));
                setGraphic(approving ? btn : null);
            }
        });
        return col;
    }

    private TableColumn<Map<String, Object>, Void> createMarkReadActionColumn(String title) {
        TableColumn<Map<String, Object>, Void> col = new TableColumn<>(title);
        col.setPrefWidth(90);
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
                    try {
                        JSONObject json = tableActionService.readMessage(String.valueOf(id));
                        if (uiFeedback.isSuccess(json)) {
                            row.put(Field.IS_READ, 1);
                            row.put(Field.STATE, 1);
                            dataTable.refresh();
                            messageLabel.setText(UiText.MSG_READ_DONE);
                        } else {
                            messageLabel.setText(uiFeedback.resolveMessage(json, UiText.MSG_READ_FAIL));
                        }
                    } catch (Exception ex) {
                        messageLabel.setText(UiText.MSG_READ_FAIL + ": " + ex.getMessage());
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Map<String, Object> row = getTableView().getItems().get(getIndex());
                Object rawRead = row.containsKey(Field.IS_READ)
                        ? row.get(Field.IS_READ)
                        : row.getOrDefault(Field.STATE, "0");
                boolean unread = !"1".equals(String.valueOf(rawRead));
                setGraphic(unread ? btn : null);
            }
        });
        return col;
    }

    private TableColumn<Map<String, Object>, Void> createPreviewFieldsActionColumn(String title, List<String> fields) {
        TableColumn<Map<String, Object>, Void> col = new TableColumn<>(title);
        col.setPrefWidth(90);
        col.setSortable(false);
        col.setReorderable(false);
        col.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button(title);
            {
                btn.setOnAction(event -> {
                    Map<String, Object> row = getTableView().getItems().get(getIndex());
                    showFieldPreview(title, row, fields);
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

    private void showFieldPreview(String title, Map<String, Object> row, List<String> fields) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);

        GridPane content = new GridPane();
        content.setHgap(12);
        content.setVgap(8);

        int line = 0;
        for (String field : fields) {
            Label keyLabel = new Label(ModuleMeta.normalizeTitle(field));
            TextArea valueArea = new TextArea(stringValue(row.get(field)));
            valueArea.setEditable(false);
            valueArea.setWrapText(true);
            valueArea.setPrefRowCount(1);
            valueArea.setMaxHeight(60);
            GridPane.setHgrow(valueArea, Priority.ALWAYS);
            content.add(keyLabel, 0, line);
            content.add(valueArea, 1, line);
            line++;
        }
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    private String stringValue(Object val) {
        return val == null ? "" : String.valueOf(val);
    }

    private TableColumn<Map<String, Object>, Void> createDownloadActionColumn() {
        TableColumn<Map<String, Object>, Void> col = new TableColumn<>(UiText.ACTION_DOWNLOAD);
        col.setPrefWidth(140);
        col.setSortable(false);
        col.setReorderable(false);
        col.setCellFactory(param -> new TableCell<>() {
            private final Button excelBtn = new Button(UiText.ACTION_DOWNLOAD_EXCEL);
            private final Button pdfBtn = new Button(UiText.ACTION_DOWNLOAD_PDF);
            private final HBox box = new HBox(6, excelBtn, pdfBtn);
            {
                excelBtn.setOnAction(event -> {
                    Map<String, Object> row = getTableView().getItems().get(getIndex());
                    Object id = row.get(Field.ID);
                    if (id == null) {
                        messageLabel.setText(UiText.MSG_RELATION_ID_NOT_FOUND);
                        return;
                    }
                    downloadRequestForm(String.valueOf(id), false);
                });
                pdfBtn.setOnAction(event -> {
                    Map<String, Object> row = getTableView().getItems().get(getIndex());
                    Object id = row.get(Field.ID);
                    if (id == null) {
                        messageLabel.setText(UiText.MSG_RELATION_ID_NOT_FOUND);
                        return;
                    }
                    downloadRequestForm(String.valueOf(id), true);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
        return col;
    }

    private TableColumn<Map<String, Object>, Void> createMatchRequestItemsActionColumn(String title) {
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
                    showRequestItemMatchingDialog(String.valueOf(id));
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

    private void showRequestItemMatchingDialog(String requestId) {
        try {
            JSONArray candidates = tableActionService.fetchRequestCandidateItems(requestId);
            if (candidates.isEmpty()) {
                messageLabel.setText(UiText.MSG_MATCH_REQUEST_ITEMS_EMPTY);
                return;
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(UiText.TITLE_MATCH_REQUEST_ITEMS);
            dialog.setHeaderText(UiText.MSG_MATCH_REQUEST_ITEMS_GUIDE);
            Window owner = dataTable != null && dataTable.getScene() != null ? dataTable.getScene().getWindow() : null;
            if (owner != null) {
                dialog.initOwner(owner);
            }

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(8);
            grid.add(new Label(UiText.FIELD_SELECT), 0, 0);
            grid.add(new Label(UiText.FIELD_CATEGORY), 1, 0);
            grid.add(new Label(UiText.FIELD_GOODS), 2, 0);
            grid.add(new Label(UiText.FIELD_SKU), 3, 0);
            grid.add(new Label(UiText.FIELD_AVAILABLE_QTY), 4, 0);
            grid.add(new Label(UiText.FIELD_MATCH_QTY), 5, 0);

            List<RequestMatchRow> matchRows = new ArrayList<>();
            for (int i = 0; i < candidates.length(); i++) {
                JSONObject candidate = candidates.getJSONObject(i);
                CheckBox selected = new CheckBox();
                selected.setSelected(candidate.optBoolean("selected", false));
                TextField quantity = new TextField(String.valueOf(candidate.optInt("requestQty", 0)));
                quantity.setPrefWidth(70);
                quantity.setDisable(!selected.isSelected());
                selected.selectedProperty().addListener((obs, oldValue, newValue) -> quantity.setDisable(!newValue));

                int row = i + 1;
                grid.add(selected, 0, row);
                grid.add(new Label(candidate.optString("categoryName")), 1, row);
                grid.add(new Label(candidate.optString("goodsName")), 2, row);
                grid.add(new Label(candidate.optString("skuCode")), 3, row);
                grid.add(new Label(String.valueOf(candidate.optInt("changeQty", 0))), 4, row);
                grid.add(quantity, 5, row);
                matchRows.add(new RequestMatchRow(candidate, selected, quantity));
            }

            ScrollPane scroll = new ScrollPane(grid);
            scroll.setFitToWidth(true);
            scroll.setPrefViewportWidth(760);
            scroll.setPrefViewportHeight(420);
            dialog.getDialogPane().setContent(scroll);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }

            JSONArray items = new JSONArray();
            for (RequestMatchRow row : matchRows) {
                if (!row.selected.isSelected()) {
                    continue;
                }
                int quantity = Integer.parseInt(row.quantity.getText().trim());
                int available = row.candidate.optInt("changeQty", 0);
                if (quantity <= 0 || quantity > available) {
                    messageLabel.setText(String.format(UiText.MSG_MATCH_REQUEST_ITEMS_QTY_INVALID, available));
                    return;
                }
                JSONObject item = new JSONObject();
                item.put("stockRecordId", row.candidate.getLong("stockRecordId"));
                item.put("requestQty", quantity);
                items.put(item);
            }

            JSONObject json = tableActionService.matchRequestItems(requestId, items);
            messageLabel.setText(uiFeedback.isSuccess(json)
                    ? UiText.MSG_MATCH_REQUEST_ITEMS_SUCCESS
                    : uiFeedback.resolveMessage(json, UiText.MSG_MATCH_REQUEST_ITEMS_FAILED));
            if (uiFeedback.isSuccess(json)) {
                loadData();
            }
        } catch (NumberFormatException ex) {
            messageLabel.setText(UiText.MSG_MATCH_REQUEST_ITEMS_NUMBER_REQUIRED);
        } catch (Exception ex) {
            messageLabel.setText(UiText.MSG_MATCH_REQUEST_ITEMS_FAILED + ": " + ex.getMessage());
        }
    }

    private void downloadRequestForm(String id, boolean pdf) {
        try {
            byte[] bytes = tableActionService.downloadRequestForm(id, pdf ? "pdf" : "excel");
            FileChooser chooser = new FileChooser();
            chooser.setTitle(UiText.TITLE_SAVE_FILE);
            chooser.setInitialFileName(String.format(pdf ? UiText.DOWNLOAD_PDF_FILENAME_PATTERN : UiText.DOWNLOAD_FILENAME_PATTERN, id));
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(pdf ? "PDF" : "Excel", pdf ? "*.pdf" : "*.xlsx"));
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
                    setControlValue(control, String.valueOf(id));
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
        selectAll.setOnAction(event -> rows.forEach(
                row -> tableRowService.selectedProperty(row, SELECTED_KEY).set(selectAll.isSelected())));
        column.setGraphic(selectAll);
        column.setEditable(true);
        column.setSortable(false);
        column.setReorderable(false);
        column.setPrefWidth(46);
        column.setMinWidth(46);
        column.setMaxWidth(46);
        column.setCellValueFactory(cell -> tableRowService.selectedProperty(cell.getValue(), SELECTED_KEY));
        column.setCellFactory(CheckBoxTableCell.forTableColumn(column));
        column.getStyleClass().add("select-column");
        return column;
    }

    private TableColumn<Map<String, Object>, String> createTextColumn(String key) {
        TableColumn<Map<String, Object>, String> col = new TableColumn<>(columnTitle(key));
        col.setPrefWidth(("id".equalsIgnoreCase(key) || (Module.GOODS.equals(currentModule) && Field.SKU_ID.equalsIgnoreCase(key))) ? 90 : 150);
        col.setEditable(!ModuleMeta.isInlineReadonlyField(key));
        col.setCellFactory(TextFieldTableCell.forTableColumn());
        col.setCellValueFactory(cell ->
                new SimpleStringProperty(tableRowService.displayCellValue(currentModule, key, cell.getValue())));
        col.setOnEditCommit(evt -> {
            if (ModuleMeta.isInlineReadonlyField(key)) {
                dataTable.refresh();
                return;
            }
            Map<String, Object> row = evt.getRowValue();
            if (row == null) {
                return;
            }
            String val = evt.getNewValue();
            if (ModuleMeta.fieldType(currentModule, key) == ModuleMeta.FieldType.SELECT) {
                ModuleMeta.Option byLabel = ModuleMeta.optionByLabel(currentModule, key, val);
                row.put(key, byLabel != null ? byLabel.value : tableRowService.normalizeEnumValue(val));
            } else {
                row.put(key, val);
            }
            dataTable.refresh();
        });
        return col;
    }

    private void setControlValue(Control control, String value) {
        if (control instanceof TextField tf) {
            tf.setText(value);
            return;
        }
        if (control instanceof ComboBox<?> combo) {
            @SuppressWarnings("unchecked")
            ComboBox<Option> cb = (ComboBox<Option>) combo;
            Option found = null;
            for (Option option : cb.getItems()) {
                if (String.valueOf(option.value).equals(value)) {
                    found = option;
                    break;
                }
            }
            if (found != null) {
                cb.setValue(found);
            } else {
                Option fallback = new Option(value, value);
                cb.getItems().add(fallback);
                cb.setValue(fallback);
            }
        }
    }

    private String columnTitle(String key) {
        if (Field.ID.equalsIgnoreCase(key)) {
            return UiText.FIELD_ID;
        }
        if (Module.GOODS.equals(currentModule) && Field.SKU_ID.equalsIgnoreCase(key)) {
            return UiText.FIELD_ID;
        }
        return ModuleMeta.normalizeTitle(key);
    }

    private List<Map<String, Object>> checkedRows() {
        return tableRowService.checkedRows(dataTable.getItems(), SELECTED_KEY);
    }

    private Map<String, Object> currentWorkingRow() {
        return tableRowService.currentWorkingRow(
                dataTable.getSelectionModel().getSelectedItem(),
                dataTable.getItems(),
                SELECTED_KEY);
    }

    private boolean isReadOnlyModule() {
        ModuleMeta.ModuleActionPolicy policy = ModuleMeta.actionPolicy(currentModule);
        return !policy.canCreate && !policy.canInlineEdit && !policy.canEdit && !policy.canBatchDelete && !policy.canDelete;
    }

    private boolean isStockOperationModule() {
        return Module.STOCK.equals(currentModule) || Module.GOODS.equals(currentModule);
    }

    private JSONObject sanitizeStockOperationPayload(JSONObject dto) {
        JSONObject clean = new JSONObject();
        putIfPresent(clean, dto, "stockId");
        putIntegerIfPresent(clean, dto, "goodsId");
        putLongIfPresent(clean, dto, "skuId");
        putIntegerIfPresent(clean, dto, "warehouseId");
        putLongIfPresent(clean, dto, "stockTypeId");
        putIntegerIfPresent(clean, dto, "quantity");
        putIntegerIfPresent(clean, dto, "sourceType");
        putLongIfPresent(clean, dto, "customerId");
        putIfPresent(clean, dto, "customerName");
        putLongIfPresent(clean, dto, "deptId");
        putIfPresent(clean, dto, "groupCode");
        putIfPresent(clean, dto, "deptCode");
        putIntegerIfPresent(clean, dto, "groupAQty");
        putIntegerIfPresent(clean, dto, "groupBQty");
        putIntegerIfPresent(clean, dto, "groupCQty");
        putIfPresent(clean, dto, "allocations");
        putIfPresent(clean, dto, "outboundMode");
        putIfPresent(clean, dto, "saleDeadline");
        putIfPresent(clean, dto, "remark");
        return clean;
    }

    private void putIfPresent(JSONObject target, JSONObject source, String key) {
        if (source == null || !source.has(key)) {
            return;
        }
        Object value = source.opt(key);
        if (value == null || value == JSONObject.NULL) {
            return;
        }
        if (value instanceof Boolean) {
            return;
        }
        if (value instanceof String text && text.isBlank()) {
            return;
        }
        target.put(key, value);
    }

    private void putIntegerIfPresent(JSONObject target, JSONObject source, String key) {
        Number number = coerceNumber(source, key);
        if (number == null) {
            return;
        }
        target.put(key, number.intValue());
    }

    private void putLongIfPresent(JSONObject target, JSONObject source, String key) {
        Number number = coerceNumber(source, key);
        if (number == null) {
            return;
        }
        target.put(key, number.longValue());
    }

    private Number coerceNumber(JSONObject source, String key) {
        if (source == null || !source.has(key)) {
            return null;
        }
        Object value = source.opt(key);
        if (value == null || value == JSONObject.NULL || value instanceof Boolean) {
            return null;
        }
        if (value instanceof Number number) {
            return number;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            if (text.contains(".")) {
                return Double.parseDouble(text);
            }
            return Long.parseLong(text);
        } catch (NumberFormatException ex) {
            return null;
        }
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

    private static final class RequestMatchRow {
        private final JSONObject candidate;
        private final CheckBox selected;
        private final TextField quantity;

        private RequestMatchRow(JSONObject candidate, CheckBox selected, TextField quantity) {
            this.candidate = candidate;
            this.selected = selected;
            this.quantity = quantity;
        }
    }
}
