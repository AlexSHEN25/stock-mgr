package co.handk.client.view;

import co.handk.client.MainApp;
import co.handk.client.model.Session;
import co.handk.client.util.ApiClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

public class MainView {

    private final BorderPane view = new BorderPane();
    private final TreeView<MenuNode> menuTree = new TreeView<>();
    private final Label titleLabel = new Label("メニューを選択してください");
    private final TableView<JSONObject> table = new TableView<>();
    private final Label pageInfo = new Label();
    private final Label msgLabel = new Label();
    private final Button prevBtn = new Button("前へ");
    private final Button nextBtn = new Button("次へ");
    private final Button refreshBtn = new Button("再読込");

    private final Map<String, JSONObject> schemaByResource = new HashMap<>();
    private String currentResource;
    private JSONObject currentSchema;
    private long page = 1;
    private long size = 20;
    private long total = 0;
    private long totalPages = 0;

    public MainView(MainApp app) {
        view.setPadding(new Insets(12));
        view.setTop(buildTopBar(app));
        view.setLeft(buildMenuArea());
        view.setCenter(buildContentArea());

        bindActions();
        loadSchemaAndMenus();
    }

    private HBox buildTopBar(MainApp app) {
        Label welcome = new Label("ようこそ, " + Session.getUsername());
        Button logoutBtn = new Button("ログアウト");
        logoutBtn.setOnAction(e -> {
            try {
                ApiClient.post("/user/logout", "{}");
            } catch (Exception ignored) {
            }
            Session.clear();
            app.showLogin();
        });

        HBox bar = new HBox(12);
        bar.setPadding(new Insets(0, 0, 10, 0));
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getChildren().addAll(welcome, spacer, logoutBtn);
        return bar;
    }

    private VBox buildMenuArea() {
        menuTree.setShowRoot(false);
        menuTree.setPrefWidth(300);
        VBox box = new VBox(new Label("メニュー"), menuTree);
        box.setSpacing(8);
        box.setPadding(new Insets(0, 12, 0, 0));
        VBox.setVgrow(menuTree, Priority.ALWAYS);
        return box;
    }

    private VBox buildContentArea() {
        HBox toolbar = new HBox(8, titleLabel, refreshBtn, prevBtn, nextBtn, pageInfo);
        VBox container = new VBox(8, toolbar, table, msgLabel);
        VBox.setVgrow(table, Priority.ALWAYS);
        return container;
    }

    private void bindActions() {
        menuTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.getValue() == null) {
                return;
            }
            MenuNode node = newVal.getValue();
            if (node.resource == null || node.resource.isBlank()) {
                return;
            }
            currentResource = node.resource;
            currentSchema = schemaByResource.get(currentResource);
            page = 1;
            titleLabel.setText(node.name);
            loadPage();
        });

        refreshBtn.setOnAction(e -> loadPage());
        prevBtn.setOnAction(e -> {
            if (page > 1) {
                page--;
                loadPage();
            }
        });
        nextBtn.setOnAction(e -> {
            if (page < totalPages) {
                page++;
                loadPage();
            }
        });
    }

    private void loadSchemaAndMenus() {
        try {
            String res = ApiClient.get("/api/schema");
            JSONArray schemas = new JSONArray(res);
            schemaByResource.clear();
            for (int i = 0; i < schemas.length(); i++) {
                JSONObject schema = schemas.getJSONObject(i);
                schemaByResource.put(schema.getString("resource"), schema);
            }
            buildMenuTree(schemas);
            msgLabel.setText("");
        } catch (Exception ex) {
            msgLabel.setText("Schema取得失敗: " + ex.getMessage());
        }
    }

    private void buildMenuTree(JSONArray schemas) {
        TreeItem<MenuNode> root = new TreeItem<>(new MenuNode("ROOT", null));
        Map<String, TreeItem<MenuNode>> topMap = new LinkedHashMap<>();
        Map<String, TreeItem<MenuNode>> subMap = new LinkedHashMap<>();

        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < schemas.length(); i++) {
            list.add(schemas.getJSONObject(i));
        }
        list.sort(Comparator.comparing(o -> o.optString("name")));

        for (JSONObject schema : list) {
            String group = schema.optString("group", "メニュー");
            String sub = schema.optString("subGroup", "");
            String topMenuId = schema.optString("topMenuId", group);
            String subMenuId = schema.optString("subMenuId", topMenuId + "/" + sub);
            String name = schema.optString("name", schema.optString("resource"));
            String resource = schema.getString("resource");

            TreeItem<MenuNode> top = topMap.computeIfAbsent(topMenuId, k -> {
                TreeItem<MenuNode> n = new TreeItem<>(new MenuNode(group, null));
                root.getChildren().add(n);
                return n;
            });

            TreeItem<MenuNode> parent = top;
            if (sub != null && !sub.isBlank()) {
                parent = subMap.computeIfAbsent(subMenuId, k -> {
                    TreeItem<MenuNode> n = new TreeItem<>(new MenuNode(sub, null));
                    top.getChildren().add(n);
                    return n;
                });
            }

            parent.getChildren().add(new TreeItem<>(new MenuNode(name, resource)));
        }

        expandAll(root);
        menuTree.setRoot(root);
    }

    private void loadPage() {
        if (currentResource == null || currentResource.isBlank()) {
            return;
        }
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("page", page);
            params.put("size", size);
            String res = ApiClient.get("/api/lowcode/" + currentResource, params);
            JSONObject json = new JSONObject(res);
            JSONArray records = json.optJSONArray("records");
            if (records == null) {
                records = new JSONArray();
            }
            total = json.optLong("total", 0);
            totalPages = json.optLong("totalPages", 0);
            buildColumns(currentSchema, records);
            table.getItems().clear();
            for (int i = 0; i < records.length(); i++) {
                table.getItems().add(records.getJSONObject(i));
            }
            pageInfo.setText("Page " + page + " / " + Math.max(totalPages, 1) + " , Total " + total);
            msgLabel.setText("");
        } catch (Exception ex) {
            msgLabel.setText("データ取得失敗: " + ex.getMessage());
        }
    }

    private void buildColumns(JSONObject schema, JSONArray records) {
        table.getColumns().clear();

        List<String> tableFields = new ArrayList<>();
        Map<String, String> titleMap = new HashMap<>();
        if (schema != null && schema.has("fields")) {
            JSONArray fields = schema.getJSONArray("fields");
            for (int i = 0; i < fields.length(); i++) {
                JSONObject f = fields.getJSONObject(i);
                String name = f.optString("name");
                boolean tableVisible = f.optBoolean("table", true);
                if (name.isBlank() || !tableVisible) {
                    continue;
                }
                tableFields.add(name);
                titleMap.put(name, f.optString("title", name));
            }
        }

        if (tableFields.isEmpty() && records.length() > 0) {
            tableFields = records.getJSONObject(0).keySet().stream().sorted().collect(Collectors.toList());
            for (String k : tableFields) {
                titleMap.put(k, k);
            }
        }

        for (String field : tableFields) {
            TableColumn<JSONObject, String> col = new TableColumn<>(titleMap.getOrDefault(field, field));
            col.setCellValueFactory(data -> {
                Object v = data.getValue().opt(field);
                return new SimpleStringProperty(v == null || v == JSONObject.NULL ? "" : String.valueOf(v));
            });
            col.setPrefWidth(160);
            table.getColumns().add(col);
        }
    }

    private void expandAll(TreeItem<?> node) {
        node.setExpanded(true);
        for (TreeItem<?> child : node.getChildren()) {
            expandAll(child);
        }
    }

    public BorderPane getView() {
        return view;
    }

    private static class MenuNode {
        private final String name;
        private final String resource;

        private MenuNode(String name, String resource) {
            this.name = name;
            this.resource = resource;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
