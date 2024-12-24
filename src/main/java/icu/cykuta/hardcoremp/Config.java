package icu.cykuta.hardcoremp;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Config {
    private FileConfiguration fileConfiguration;
    private final String fileName = "config.yml";
    private final java.io.File dataFolder = HardcoreMP.getPlugin().getDataFolder();
    private java.io.File file;

    public Config() throws IOException, InvalidConfigurationException {
        this.register();
    }

    public void register() throws IOException, InvalidConfigurationException {
        this.file = new File(dataFolder, fileName);
        if (!this.file.exists()) {
            HardcoreMP.getPlugin().saveResource(fileName, false);
        }

        this.fileConfiguration = new YamlConfiguration();
        this.fileConfiguration.load(this.file);
    }

    public FileConfiguration getFileConfiguration() {
        return this.fileConfiguration;
    }

    public void save() {
        try {
            this.fileConfiguration.save(this.file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}