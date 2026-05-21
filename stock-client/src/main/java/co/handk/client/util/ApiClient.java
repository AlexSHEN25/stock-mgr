package co.handk.client.util;

import co.handk.client.model.Session;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApiClient {
    private static final Logger LOGGER = Logger.getLogger(ApiClient.class.getName());

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_DELETE = "DELETE";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    private static final String HEADER_X_LANG = "X-Lang";
    private static final String LANG_JA_JP = "ja-JP";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String AUTH_BEARER_PREFIX = "Bearer ";
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int LOGIN_TIMEOUT_CODE = 401;
    private static final String JSON_CODE = "code";
    private static final String EMPTY_RESPONSE_MESSAGE = "empty response";
    private static final String QUERY_PREFIX = "?";
    private static final String QUERY_SEPARATOR = "&";
    private static final String QUERY_ASSIGN = "=";
    private static final String EMPTY = "";
    private static final String DEBUG_FLAG = "stock.client.http.debug";
    private static final AtomicBoolean LOGIN_TIMEOUT_HANDLED = new AtomicBoolean(false);
    private static Runnable loginTimeoutHandler;
    private static final Map<String, String> PATH_ALIASES = new LinkedHashMap<>();

    static {
        PATH_ALIASES.put("/goodsManagement/", "/goods/");
        PATH_ALIASES.put("/goods/management/page", "/goods/page");
    }

    public static void setLoginTimeoutHandler(Runnable handler) {
        loginTimeoutHandler = handler;
    }

    public static String post(String path, String json) throws Exception {
        String normalizedPath = normalizePath(path);
        HttpURLConnection conn = open(normalizedPath, METHOD_POST);
        conn.setRequestProperty(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
        conn.setDoOutput(true);

        long start = System.currentTimeMillis();
        debugRequest(METHOD_POST, normalizedPath, json, conn);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        return read(conn, start, METHOD_POST, normalizedPath);
    }

    public static String put(String path, String json) throws Exception {
        String normalizedPath = normalizePath(path);
        HttpURLConnection conn = open(normalizedPath, METHOD_PUT);
        conn.setRequestProperty(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
        conn.setDoOutput(true);

        long start = System.currentTimeMillis();
        debugRequest(METHOD_PUT, normalizedPath, json, conn);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        return read(conn, start, METHOD_PUT, normalizedPath);
    }

    public static String get(String path, Map<String, String> queryParams) throws Exception {
        String normalizedPath = normalizePath(path);
        String fullPath = normalizedPath + toQueryString(queryParams);
        HttpURLConnection conn = open(fullPath, METHOD_GET);

        long start = System.currentTimeMillis();
        debugRequest(METHOD_GET, fullPath, EMPTY, conn);
        return read(conn, start, METHOD_GET, fullPath);
    }

    public static String delete(String path) throws Exception {
        String normalizedPath = normalizePath(path);
        HttpURLConnection conn = open(normalizedPath, METHOD_DELETE);

        long start = System.currentTimeMillis();
        debugRequest(METHOD_DELETE, normalizedPath, EMPTY, conn);
        return read(conn, start, METHOD_DELETE, normalizedPath);
    }

    public static byte[] getBytes(String path) throws Exception {
        String normalizedPath = normalizePath(path);
        HttpURLConnection conn = open(normalizedPath, METHOD_GET);
        long start = System.currentTimeMillis();
        debugRequest(METHOD_GET, normalizedPath, EMPTY, conn);
        int status = conn.getResponseCode();
        InputStream is = status >= HTTP_BAD_REQUEST ? conn.getErrorStream() : conn.getInputStream();
        if (is == null) {
            throw new IOException(EMPTY_RESPONSE_MESSAGE);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = is.read(buffer)) > 0) {
            bos.write(buffer, 0, len);
        }
        byte[] bytes = bos.toByteArray();
        debugResponse(METHOD_GET, normalizedPath, status, System.currentTimeMillis() - start, "binary(" + bytes.length + ")");
        if (status >= HTTP_BAD_REQUEST) {
            String body = new String(bytes, StandardCharsets.UTF_8);
            throw new IOException(body.isBlank() ? ("HTTP " + status) : body);
        }
        return bytes;
    }

    private static HttpURLConnection open(String path, String method) throws Exception {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty(HEADER_ACCEPT_LANGUAGE, LANG_JA_JP);
        conn.setRequestProperty(HEADER_X_LANG, LANG_JA_JP);

        String token = Session.getToken();
        if (token != null && !token.isBlank()) {
            conn.setRequestProperty(HEADER_AUTHORIZATION, AUTH_BEARER_PREFIX + token);
        }
        return conn;
    }

    private static String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return path;
        }
        String normalized = path;
        for (Map.Entry<String, String> entry : PATH_ALIASES.entrySet()) {
            normalized = normalized.replace(entry.getKey(), entry.getValue());
        }
        return normalized;
    }

    private static String toQueryString(Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return EMPTY;
        }

        StringBuilder sb = new StringBuilder(QUERY_PREFIX);
        boolean first = true;
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            if (!first) {
                sb.append(QUERY_SEPARATOR);
            }
            first = false;
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append(QUERY_ASSIGN);
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        return first ? EMPTY : sb.toString();
    }

    private static String read(HttpURLConnection conn, long start, String method, String path) throws Exception {
        int status = conn.getResponseCode();
        InputStream is = status >= HTTP_BAD_REQUEST ? conn.getErrorStream() : conn.getInputStream();
        if (is == null) {
            throw new IOException(EMPTY_RESPONSE_MESSAGE);
        }

        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        String body = result.toString();
        debugResponse(method, path, status, System.currentTimeMillis() - start, body);

        try {
            JSONObject json = new JSONObject(body);
            if (json.optInt(JSON_CODE) == LOGIN_TIMEOUT_CODE) {
                if (isDebugEnabled()) {
                    System.out.println("[AUTH] 401 received, messageKey=" + json.optString("messageKey") + ", message=" + json.optString("message"));
                }
                Session.clear();
                if (loginTimeoutHandler != null && LOGIN_TIMEOUT_HANDLED.compareAndSet(false, true)) {
                    loginTimeoutHandler.run();
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, "Non-JSON response, skip auth code parsing. body=" + body, ex);
        }
        return body;
    }

    private static void debugRequest(String method, String path, String body, HttpURLConnection conn) {
        if (!isDebugEnabled()) {
            return;
        }
        String auth = conn.getRequestProperty(HEADER_AUTHORIZATION);
        String maskedAuth = (auth == null || auth.isBlank()) ? "" : AUTH_BEARER_PREFIX + "***";
        System.out.println("[HTTP-REQ] " + method + " " + BASE_URL + path);
        System.out.println("[HTTP-REQ] headers={" + HEADER_ACCEPT_LANGUAGE + "=" + conn.getRequestProperty(HEADER_ACCEPT_LANGUAGE)
                + ", " + HEADER_X_LANG + "=" + conn.getRequestProperty(HEADER_X_LANG)
                + ", " + HEADER_CONTENT_TYPE + "=" + conn.getRequestProperty(HEADER_CONTENT_TYPE)
                + ", " + HEADER_AUTHORIZATION + "=" + maskedAuth + "}");
        if (body != null && !body.isBlank()) {
            System.out.println("[HTTP-REQ] body=" + body);
        }
    }

    private static void debugResponse(String method, String path, int status, long costMs, String body) {
        if (!isDebugEnabled()) {
            return;
        }
        System.out.println("[HTTP-RES] " + method + " " + BASE_URL + path + " status=" + status + " cost=" + costMs + "ms");
        System.out.println("[HTTP-RES] body=" + body);
    }

    private static boolean isDebugEnabled() {
        return "true".equalsIgnoreCase(System.getProperty(DEBUG_FLAG));
    }

    public static void resetLoginTimeoutHandled() {
        LOGIN_TIMEOUT_HANDLED.set(false);
    }
}
