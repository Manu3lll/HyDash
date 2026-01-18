package de.manu3lll.hydash.HttpHandler;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.Universe;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class DashboardPlayerCountHelper implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {

        t.getResponseHeaders().add("Content-Type", "text/event-stream");
        t.getResponseHeaders().add("Cache-Control", "no-cache");
        t.getResponseHeaders().add("Connection", "keep-alive");
        t.sendResponseHeaders(200, 0);
        OutputStream os = t.getResponseBody();
        int capacity = HytaleServer.get().getConfig().getMaxPlayers();
        try {
            while (true) {
                int player = Universe.get().getPlayerCount();
                String msg = "data: " + player + "/"+capacity+"\n\n";
                os.write(msg.getBytes(StandardCharsets.UTF_8));
                os.flush();
                Thread.sleep(1000);
            }
        } catch (Exception e) {
        } finally {
            os.close();
        }

    }
}
