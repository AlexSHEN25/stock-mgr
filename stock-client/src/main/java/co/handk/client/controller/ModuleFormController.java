package co.handk.client.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.*;

public class ModuleFormController {

    @FXML private Label titleLabel;
    @FXML private Label errorLabel;
    @FXML private TextField idField;
    @FXML private TextField usernameField;
    @FXML private TextField deptIdField;
    @FXML private TextField passwordField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField nameField;
    @FXML private TextField codeField;
    @FXML private TextField addressField;
    @FXML private TextField managerIdField;
    @FXML private TextField goodsIdField;
    @FXML private TextField goodsNameField;
    @FXML private TextField skuIdField;
    @FXML private TextField skuCodeField;
    @FXML private TextField warehouseIdField;
    @FXML private TextField currentQtyField;
    @FXML private TextField lockQtyField;
    @FXML private TextField priceField;
    @FXML private TextField currencyField;
    @FXML private TextField stockTypeIdField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Button saveButton;

    private boolean submitted;
    private String module;
    private boolean editMode;

    private final Map<String, TextField> fields = new LinkedHashMap<>();

    @FXML
    private void initialize() {
        fields.put("id", idField);
        fields.put("username", usernameField);
        fields.put("deptId", deptIdField);
        fields.put("password", passwordField);
        fields.put("email", emailField);
        fields.put("phone", phoneField);
        fields.put("name", nameField);
        fields.put("code", codeField);
        fields.put("address", addressField);
        fields.put("managerId", managerIdField);
        fields.put("goodsId", goodsIdField);
        fields.put("goodsName", goodsNameField);
        fields.put("skuId", skuIdField);
        fields.put("skuCode", skuCodeField);
        fields.put("warehouseId", warehouseIdField);
        fields.put("currentQty", currentQtyField);
        fields.put("lockQty", lockQtyField);
        fields.put("price", priceField);
        fields.put("currency", currencyField);
        fields.put("stockTypeId", stockTypeIdField);

        statusCombo.setItems(FXCollections.observableArrayList("启用", "禁用"));
    }

    public void configure(String module, String title, boolean editMode, Map<String, Object> source) {
        this.module = module;
        this.editMode = editMode;
        titleLabel.setText(title);

        Set<String> visibleKeys = visibleKeysByModule(module, editMode);
        fields.forEach((key, field) -> {
            boolean visible = visibleKeys.contains(key);
            Node label = findLabelForField(field);
            field.setVisible(visible);
            field.setManaged(visible);
            if (label != null) {
                label.setVisible(visible);
                label.setManaged(visible);
            }
        });

        boolean showStatus = visibleKeys.contains("status");
        Node statusLabel = findLabelForNode(statusCombo);
        statusCombo.setVisible(showStatus);
        statusCombo.setManaged(showStatus);
        if (statusLabel != null) {
            statusLabel.setVisible(showStatus);
            statusLabel.setManaged(showStatus);
        }

        if (source != null) {
            source.forEach((k, v) -> {
                if ("status".equals(k) && v != null) {
                    statusCombo.setValue("ENABLE".equalsIgnoreCase(String.valueOf(v)) ? "启用" : "禁用");
                    return;
                }
                TextField field = fields.get(k);
                if (field != null && v != null) {
                    field.setText(String.valueOf(v));
                }
            });
        } else {
            applyDefaultTemplate(module);
        }
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public JSONObject toJson() {
        JSONObject dto = new JSONObject();

        switch (module) {
            case "user" -> {
                putIfNotBlank(dto, "id", idField, Long::parseLong);
                putIfNotBlank(dto, "username", usernameField, s -> s);
                putIfNotBlank(dto, "deptId", deptIdField, Long::parseLong);
                putIfNotBlank(dto, "password", passwordField, s -> s);
                putIfNotBlank(dto, "email", emailField, s -> s);
                putIfNotBlank(dto, "phone", phoneField, s -> s);
                putStatus(dto);
            }
            case "stock" -> {
                putIfNotBlank(dto, "id", idField, Long::parseLong);
                putIfNotBlank(dto, "goodsId", goodsIdField, Integer::parseInt);
                putIfNotBlank(dto, "goodsName", goodsNameField, s -> s);
                putIfNotBlank(dto, "skuId", skuIdField, Long::parseLong);
                putIfNotBlank(dto, "skuCode", skuCodeField, s -> s);
                putIfNotBlank(dto, "warehouseId", warehouseIdField, Integer::parseInt);
                putIfNotBlank(dto, "currentQty", currentQtyField, Integer::parseInt);
                putIfNotBlank(dto, "lockQty", lockQtyField, Integer::parseInt);
                putIfNotBlank(dto, "price", priceField, BigDecimal::new);
                putIfNotBlank(dto, "currency", currencyField, s -> s);
                putIfNotBlank(dto, "stockTypeId", stockTypeIdField, Long::parseLong);
                putStatus(dto);
            }
            case "warehouse" -> {
                putIfNotBlank(dto, "id", idField, Long::parseLong);
                putIfNotBlank(dto, "name", nameField, s -> s);
                putIfNotBlank(dto, "code", codeField, s -> s);
                putIfNotBlank(dto, "address", addressField, s -> s);
                putIfNotBlank(dto, "managerId", managerIdField, Long::parseLong);
                putStatus(dto);
            }
            case "stockType" -> {
                putIfNotBlank(dto, "id", idField, Long::parseLong);
                putIfNotBlank(dto, "name", nameField, s -> s);
                putStatus(dto);
            }
            default -> {
            }
        }
        return dto;
    }

    @FXML
    private void onSave() {
        errorLabel.setText("");
        try {
            JSONObject dto = toJson();
            if (editMode && !dto.has("id")) {
                errorLabel.setText("编辑模式必须填写ID");
                return;
            }
            submitted = true;
            saveButton.getScene().getWindow().hide();
        } catch (Exception ex) {
            errorLabel.setText("输入格式错误: " + ex.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        submitted = false;
        saveButton.getScene().getWindow().hide();
    }

    private void applyDefaultTemplate(String module) {
        if ("user".equals(module)) {
            usernameField.setText("new_user");
            deptIdField.setText("1");
            passwordField.setText("123456");
            emailField.setText("new_user@example.com");
            phoneField.setText("13800000000");
        } else if ("stock".equals(module)) {
            goodsIdField.setText("1");
            goodsNameField.setText("sample goods");
            skuIdField.setText("1");
            skuCodeField.setText("SKU-001");
            warehouseIdField.setText("1");
            currentQtyField.setText("100");
            lockQtyField.setText("0");
            priceField.setText("10.5");
            currencyField.setText("CNY");
            stockTypeIdField.setText("1");
        } else if ("warehouse".equals(module)) {
            nameField.setText("sample warehouse");
            codeField.setText("WH-001");
            addressField.setText("sample address");
            managerIdField.setText("1");
        } else if ("stockType".equals(module)) {
            nameField.setText("normal");
        }
    }

    private void putStatus(JSONObject dto) {
        String statusText = statusCombo.getValue();
        if (statusText == null || statusText.isBlank()) {
            return;
        }
        dto.put("status", "启用".equals(statusText) ? "ENABLE" : "DISABLE");
    }

    private interface Parser {
        Object parse(String s);
    }

    private void putIfNotBlank(JSONObject dto, String key, TextField field, Parser parser) {
        if (field == null || field.getText() == null || field.getText().isBlank()) {
            return;
        }
        dto.put(key, parser.parse(field.getText().trim()));
    }

    private Set<String> visibleKeysByModule(String module, boolean editMode) {
        Set<String> keys = new LinkedHashSet<>();
        if (editMode) {
            keys.add("id");
            keys.add("status");
        }

        switch (module) {
            case "user" -> Collections.addAll(keys, "username", "deptId", "password", "email", "phone");
            case "stock" -> Collections.addAll(keys, "goodsId", "goodsName", "skuId", "skuCode", "warehouseId", "currentQty", "lockQty", "price", "currency", "stockTypeId");
            case "warehouse" -> Collections.addAll(keys, "name", "code", "address", "managerId");
            case "stockType" -> keys.add("name");
            default -> {
            }
        }
        return keys;
    }

    private Node findLabelForField(TextField field) {
        return findLabelForNode(field);
    }

    private Node findLabelForNode(Node node) {
        if (node == null || node.getParent() == null) {
            return null;
        }
        Integer row = javafx.scene.layout.GridPane.getRowIndex(node);
        if (row == null) {
            row = 0;
        }
        for (Node n : node.getParent().getChildrenUnmodifiable()) {
            Integer nRow = javafx.scene.layout.GridPane.getRowIndex(n);
            Integer nCol = javafx.scene.layout.GridPane.getColumnIndex(n);
            if (Objects.equals(nRow, row) && Objects.equals(nCol, 0)) {
                return n;
            }
        }
        return null;
    }
}
