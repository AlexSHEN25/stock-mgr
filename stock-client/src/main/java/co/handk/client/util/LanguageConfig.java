package co.handk.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class LanguageConfig {

    private static final String KEY_LANG = "lang";
    private static final String DEFAULT_LANG = "ja-JP";
    private static final Path CONFIG_PATH = Paths.get(
            System.getProperty("user.home"), ".stock-client", "config.properties"
    );

    private LanguageConfig() {
    }

    public static String getLanguage() {
        Properties properties = load();
        String lang = properties.getProperty(KEY_LANG, DEFAULT_LANG);
        return (lang == null || lang.isBlank()) ? DEFAULT_LANG : lang.trim();
    }

    public static void setLanguage(String language) {
        String value = (language == null || language.isBlank()) ? DEFAULT_LANG : language.trim();
        Properties properties = load();
        properties.setProperty(KEY_LANG, value);
        store(properties);
    }

    private static Properties load() {
        Properties properties = new Properties();
        if (!Files.exists(CONFIG_PATH)) {
            return properties;
        }
        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            properties.load(input);
        } catch (IOException ignored) {
        }
        return properties;
    }

    private static void store(Properties properties) {
        try {
            Path parent = CONFIG_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(output, "stock-client settings");
            }
        } catch (IOException ignored) {
        }
    }
}

