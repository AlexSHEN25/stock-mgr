package co.handk.client.util;

import co.handk.client.model.Session;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080";
    private static Runnable loginTimeoutHandler;

    public static void setLoginTimeoutHandler(Runnable handler) {
        loginTimeoutHandler = handler;
    }

    public static String post(String path, String json) throws Exception {
        HttpURLConnection conn = open(path, "POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        return read(conn);
    }

    public static String put(String path, String json) throws Exception {
        HttpURLConnection conn = open(path, "PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        return read(conn);
    }

    public static String get(String path, Map<String, String> queryParams) throws Exception {
        String fullPath = path + toQueryString(queryParams);
        HttpURLConnection conn = open(fullPath, "GET");
        return read(conn);
    }

    public static String delete(String path) throws Exception {
        HttpURLConnection conn = open(path, "DELETE");
        return read(conn);
    }

    private static HttpURLConnection open(String path, String method) throws Exception {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        String language = LanguageConfig.getLanguage();
        conn.setRequestProperty("Accept-Language", language);
        conn.setRequestProperty("X-Lang", language);

        String token = Session.getToken();
        if (token != null && !token.isBlank()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        return conn;
    }

    private static String toQueryString(Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder("?");
        boolean first = true;
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            if (!first) {
                sb.append("&");
            }
            first = false;
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        return first ? "" : sb.toString();
    }

    private static String read(HttpURLConnection conn) throws Exception {
        InputStream is = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (is == null) {
            throw new IOException("empty response");
        }

        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        String body = result.toString();
        try {
            JSONObject json = new JSONObject(body);
            if (json.optInt("code") == 401) {
                Session.clear();
                if (loginTimeoutHandler != null) {
                    loginTimeoutHandler.run();
                }
            }
        } catch (Exception ignored) {
            // Ignore non-JSON responses.
        }
        return body;
    }
}
