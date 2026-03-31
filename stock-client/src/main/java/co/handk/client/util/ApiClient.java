package co.handk.client.util;

import co.handk.client.model.Session;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080";
    private static Runnable loginTimeoutHandler;

    public static void setLoginTimeoutHandler(Runnable handler) {
        loginTimeoutHandler = handler;
    }

    public static String post(String path, String json) throws Exception {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        String token = Session.getToken();
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        return read(conn);
    }

    private static String read(HttpURLConnection conn) throws Exception {
        InputStream is = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (is == null) {
            throw new IOException("empty response");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder result = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            result.append(line);
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
            // 非标准JSON响应，忽略统一登录态处理
        }
        return body;
    }
}
