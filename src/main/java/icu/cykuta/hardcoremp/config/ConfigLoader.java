package icu.cykuta.hardcoremp.config;

import icu.cykuta.hardcoremp.HardcoreMP;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigLoader {
    private FileConfiguration fileConfiguration;
    private final File dataFolder = HardcoreMP.getPlugin().getDataFolder();
    private File file;

    /**
     * Register the config file.
     *
     * @throws IOException                   If an I/O error occurs.
     * @throws InvalidConfigurationException    If the file is invalid.
     */
    public void register() throws IOException, InvalidConfigurationException {
        String fileName = "config.yml";
        this.file = new File(dataFolder, fileName);
        if (!this.file.exists()) {
            HardcoreMP.getPlugin().saveResource(fileName, false);
        }

        this.fileConfiguration = new YamlConfiguration();
        this.fileConfiguration.load(this.file);
    }

    /**
     * Get the file configuration.
     *
     * @return The file configuration.
     */
    public FileConfiguration getFileConfiguration() {
        return this.fileConfiguration;
    }

    /**
     * Save the config file.
     */
    public void save() {
        try {
            this.fileConfiguration.save(this.file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reload the config file.
     */
    public void reload() {
        try {
            this.fileConfiguration.load(this.file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

}