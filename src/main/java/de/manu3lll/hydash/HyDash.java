package de.manu3lll.hydash;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
public class HyDash extends JavaPlugin {

    private SimpleWebServer webServer;

    public HyDash(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // Starte unseren eigenen Server
        webServer = new SimpleWebServer(this);
        webServer.start();
    }

    @Override
    protected void shutdown() {
        if (webServer != null) {
            webServer.stop();
        }
    }
}