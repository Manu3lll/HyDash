package de.manu3lll.hydash;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HelperMethods {

    public static boolean checkAuth(HttpExchange t, String AUTH_TOKEN) throws IOException {
        String query = t.getRequestURI().getQuery();
        if (query != null && query.contains("token=" + AUTH_TOKEN)) {
            return true;
        }
        sendResponse(t, "Access denied! Wrong Token.", 403);
        return false;
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

            // Wir suchen einfach "latest.log" oder die neueste
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

