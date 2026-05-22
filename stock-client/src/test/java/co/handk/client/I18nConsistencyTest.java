package co.handk.client;

import co.handk.client.util.ModuleMeta;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class I18nConsistencyTest {

    private static final Pattern FXML_KEY_PATTERN = Pattern.compile("%([a-zA-Z0-9._-]+)");

    @Test
    void uiAndUiJaHaveSameKeys() throws Exception {
        Properties base = loadProps("i18n/ui.properties");
        Properties ja = loadProps("i18n/ui_ja.properties");

        Set<String> missingInJa = new LinkedHashSet<>(base.stringPropertyNames());
        missingInJa.removeAll(ja.stringPropertyNames());

        Set<String> extraInJa = new LinkedHashSet<>(ja.stringPropertyNames());
        extraInJa.removeAll(base.stringPropertyNames());

        assertTrue(missingInJa.isEmpty(), "Missing keys in ui_ja.properties: " + missingInJa);
        assertTrue(extraInJa.isEmpty(), "Extra keys in ui_ja.properties: " + extraInJa);
    }

    @Test
    void fxmlReferencedKeysMustExistInUiProperties() throws Exception {
        Properties base = loadProps("i18n/ui.properties");
        Set<String> available = base.stringPropertyNames();

        List<String> fxmls = List.of("fxml/login.fxml", "fxml/main.fxml");
        Set<String> missing = new LinkedHashSet<>();

        for (String fxmlPath : fxmls) {
            String content = loadText(fxmlPath);
            Matcher matcher = FXML_KEY_PATTERN.matcher(content);
            while (matcher.find()) {
                String key = matcher.group(1);
                if (!available.contains(key)) {
                    missing.add(key + " (from " + fxmlPath + ")");
                }
            }
        }

        assertTrue(missing.isEmpty(), "Missing i18n keys referenced by FXML: " + missing);
    }

    @Test
    void moduleFieldsMustHaveFieldI18nKeys() throws Exception {
        Properties base = loadProps("i18n/ui.properties");
        Set<String> available = base.stringPropertyNames();

        List<String> modules = List.of(
                "user", "dept", "warehouse", "role", "permission",
                "goods", "goodsLevelPrice", "maker", "brand", "category", "series",
                "stock", "stockType", "stockRecord", "stockOrder", "stockOrderItem",
                "requestForm", "requestItem", "customer", "customerLevel",
                "config", "message", "operateLog", "goodsSku", "goodsSkuSpec",
                "goodsImage", "userRole", "rolePermission", "userToken"
        );

        Set<String> requiredKeys = new LinkedHashSet<>();
        for (String module : modules) {
            requiredKeys.add("field.id");
            for (String field : ModuleMeta.queryFields(module)) {
                requiredKeys.add("field." + field);
            }
            for (String field : ModuleMeta.formFields(module)) {
                requiredKeys.add("field." + field);
            }
        }

        List<String> missing = new ArrayList<>();
        for (String key : requiredKeys) {
            if (!available.contains(key)) {
                missing.add(key);
            }
        }

        assertTrue(missing.isEmpty(), "Missing field i18n keys: " + missing);
    }

    private static Properties loadProps(String path) throws Exception {
        Properties props = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            Objects.requireNonNull(in, "Resource not found: " + path);
            props.load(in);
        }
        return props;
    }

    private static String loadText(String path) throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            Objects.requireNonNull(in, "Resource not found: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
