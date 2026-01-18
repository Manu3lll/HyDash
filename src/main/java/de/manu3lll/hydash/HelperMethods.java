package de.manu3lll.hydash;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HelperMethods {

    public static boolean checkAuth(HttpExchange t, WebConfig config) throws IOException {
        String query = t.getRequestURI().getQuery();
        if (query != null && query.contains("token=" + config.token)) {
            return true;
        }

        String cookieHeader = t.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                String[] parts = cookie.trim().split("=");
                if (parts.length == 2) {
                    String name = parts[0];
                    String value = parts[1];
                    if ("auth_token".equals(name) && config.token.equals(value)) {
                        return true;
                    }
                }
            }
        }

        if (!t.getRequestURI().getPath().equals("/dashboard")) {
            sendResponse(t, "Access denied! Login required.", 403);
            return false;
        }
        return true;
    }

    public static void sendResponse(HttpExchange t, String response, int code) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(code, bytes.length);
        OutputStream os = t.getResponseBody();
        os.write(bytes);
        os.close();
    }

    public static Path getLatestLogFile() {
        try {
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) return null;

            Path latest = logDir.resolve("latest.log");
            if (Files.exists(latest)) return latest;

            return Files.list(logDir)
                    .filter(path -> path.toString().endsWith(".log"))
                    .max((p1, p2) -> {
                        try { return Files.getLastModifiedTime(p1).compareTo(Files.getLastModifiedTime(p2)); }
                        catch (IOException e) { return 0; }
                    }).orElse(null);
        } catch(IOException e) { return null; }
    }
}

