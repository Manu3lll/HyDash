package de.manu3lll.hydash.HttpHandler;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.manu3lll.hydash.HelperMethods;
import de.manu3lll.hydash.SimpleWebServer;
import de.manu3lll.hydash.WebConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class LogStreamHandler extends SimpleWebServer implements HttpHandler {
    public LogStreamHandler(JavaPlugin plugin, WebConfig config) {
        super(plugin, config);
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (!HelperMethods.checkAuth(t, this.config)) return;

        t.getResponseHeaders().add("Content-Type", "text/event-stream");
        t.getResponseHeaders().add("Cache-Control", "no-cache");
        t.getResponseHeaders().add("Connection", "keep-alive");
        t.sendResponseHeaders(200, 0);

        OutputStream os = t.getResponseBody();

        Path logFile = HelperMethods.getLatestLogFile();
        if (logFile == null) {
            String msg = "data: No log file found (logs/?).\n\n";
            os.write(msg.getBytes(StandardCharsets.UTF_8));
            os.close();
            return;
        }

        long lastPosition = Files.size(logFile);
        if (lastPosition > 1000) lastPosition -= 1000;
        else lastPosition = 0;

        try {
            while (true) {
                long currentSize = Files.size(logFile);

                if (currentSize > lastPosition) {
                    try (RandomAccessFile raf = new RandomAccessFile(logFile.toFile(), "r")) {
                        raf.seek(lastPosition);
                        String line;
                        while ((line = raf.readLine()) != null) {
                            String sseMessage = "data: " + line + "\n\n";
                            os.write(sseMessage.getBytes(StandardCharsets.UTF_8));
                        }
                        lastPosition = raf.getFilePointer();
                        os.flush();
                    }
                } else if (currentSize < lastPosition) {
                    lastPosition = 0;
                    logFile = HelperMethods.getLatestLogFile();
                }

                Thread.sleep(500);
            }
        } catch (Exception e) {
            //plugin.getLogger().atInfo().log("Dashboard connection closed.");
        } finally {
            os.close();
        }
    }
}
