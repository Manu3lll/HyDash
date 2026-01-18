package de.manu3lll.hydash;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private final JavaPlugin plugin;
    private final Gson gson;
    private WebConfig config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public WebConfig load() {
        Path dataFolder = plugin.getDataDirectory();
        Path configFile = dataFolder.resolve("config.json");

        try {
            if (!Files.exists(dataFolder)) {
                Files.createDirectories(dataFolder);
            }

            if (!Files.exists(configFile)) {
                this.config = new WebConfig();
                save(configFile);
                plugin.getLogger().atInfo().log("Created default config: " + configFile);
            } else {
                try (Reader reader = Files.newBufferedReader(configFile)) {
                    this.config = gson.fromJson(reader, WebConfig.class);
                    plugin.getLogger().atInfo().log("Config sucessfully loaded!");
                }
            }

        } catch (Exception e) {
            plugin.getLogger().atWarning().log("Error while loading values of config.json file! Using default values.", e);
            this.config = new WebConfig();
        }

        return this.config;
    }

    private void save(Path file) {
        try (Writer writer = Files.newBufferedWriter(file)) {
            gson.toJson(this.config, writer);
        } catch (Exception e) {
            plugin.getLogger().atWarning().log("Could not save values into config.json file, please check file permissions!", e);
        }
    }

    public WebConfig getConfig() {
        return config;
    }
}