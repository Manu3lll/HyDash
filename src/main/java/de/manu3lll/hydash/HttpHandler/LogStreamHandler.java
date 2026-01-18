package de.manu3lll.hydash.HttpHandler;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.manu3lll.hydash.HelperMethods;
import de.manu3lll.hydash.SimpleWebServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class LogStreamHandler extends SimpleWebServer implements HttpHandler {
    public LogStreamHandler(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (!HelperMethods.checkAuth(t, this.AUTH_TOKEN)) return;

        // WICHTIG: SSE Header
        t.getResponseHeaders().add("Content-Type", "text/event-stream");
        t.getResponseHeaders().add("Cache-Control", "no-cache");
        t.getResponseHeaders().add("Connection", "keep-alive");
        t.sendResponseHeaders(200, 0);

        OutputStream os = t.getResponseBody();

        // Datei suchen
        Path logFile = HelperMethods.getLatestLogFile();
        if (logFile == null) {
            // SSE Format beachten: "data: Nachricht\n\n"
            String msg = "data: No log file found (logs/?).\n\n";
            os.write(msg.getBytes(StandardCharsets.UTF_8));
            os.close();
            return;
        }

        // Wir starten am Ende der Datei (damit wir nicht 1000 alte Zeilen laden)
        long lastPosition = Files.size(logFile);
        // Optional: Wenn du die letzten Zeilen sehen willst, zieh hier z.B. 1000 Bytes ab
        if (lastPosition > 1000) lastPosition -= 1000;
        else lastPosition = 0;

        try {
            // Endlos-Schleife, die prüft ob neue Zeilen da sind
            while (true) {
                long currentSize = Files.size(logFile);

                if (currentSize > lastPosition) {
                    try (RandomAccessFile raf = new RandomAccessFile(logFile.toFile(), "r")) {
                        raf.seek(lastPosition);
                        String line;
                        while ((line = raf.readLine()) != null) {
                            // WICHTIG: Das SSE Protokoll verlangt "data: " am Anfang!
                            String sseMessage = "data: " + line + "\n\n";
                            os.write(sseMessage.getBytes(StandardCharsets.UTF_8));
                        }
                        lastPosition = raf.getFilePointer();
                        os.flush(); // Daten sofort zum Browser schieben
                    }
                } else if (currentSize < lastPosition) {
                    // Log Rotation (Datei wurde neu angelegt)
                    lastPosition = 0;
                    logFile = HelperMethods.getLatestLogFile();
                }

                // Kurze Pause um CPU zu sparen
                Thread.sleep(500);

                // Optional: Heartbeat senden, damit Verbindung nicht abbricht
                // os.write(": heartbeat\n\n".getBytes());
                // os.flush();
            }
        } catch (Exception e) {
            // Browser hat Fenster geschlossen
            plugin.getLogger().atInfo().log("Dashboard connection closed.");
        } finally {
            os.close();
        }
    }
}
