package de.manu3lll.hydash;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
public class HyDash extends JavaPlugin {

    private SimpleWebServer webServer;

    public HyDash(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        ConfigManager configManager = new ConfigManager(this);
        WebConfig config = configManager.load();
        webServer = new SimpleWebServer(this, config);
        webServer.start();
    }

    @Override
    protected void shutdown() {
        if (webServer != null) {
            webServer.stop();
        }
    }
}