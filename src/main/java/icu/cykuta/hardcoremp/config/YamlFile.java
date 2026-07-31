package icu.cykuta.hardcoremp.config;

import icu.cykuta.hardcoremp.HardcoreMP;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * A YAML file stored in the plugin data folder.
 */
public class YamlFile {
    private final String fileName;
    private FileConfiguration fileConfiguration;
    private File file;

    public YamlFile(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Copy the default file from the jar if it is missing and load it.
     *
     * @throws IOException                   If an I/O error occurs.
     * @throws InvalidConfigurationException If the file is invalid.
     */
    public void register() throws IOException, InvalidConfigurationException {
        this.file = new File(HardcoreMP.getPlugin().getDataFolder(), fileName);
        if (!this.file.exists()) {
            HardcoreMP.getPlugin().saveResource(fileName, false);
        }

        this.fileConfiguration = new YamlConfiguration();
        this.fileConfiguration.load(this.file);
    }

    /**
     * Open a YAML file by path, creating it empty when it does not exist.
     * Unlike {@link #register()} this does not need a running server.
     *
     * @param file the file to read and write
     * @return the loaded file
     */
    public static YamlFile open(File file) throws IOException, InvalidConfigurationException {
        YamlFile yaml = new YamlFile(file.getName());
        yaml.file = file;

        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Could not create " + file);
        }

        yaml.fileConfiguration = new YamlConfiguration();
        yaml.fileConfiguration.load(file);
        return yaml;
    }

    /**
     * Get the file configuration.
     *
     * @return The file configuration.
     */
    public FileConfiguration getFileConfiguration() {
        return this.fileConfiguration;
    }

    public String getFileName() {
        return this.fileName;
    }

    /**
     * Write the in-memory values back to disk.
     */
    public void save() {
        try {
            this.fileConfiguration.save(this.file);
        } catch (IOException e) {
            throw new RuntimeException("Could not save " + fileName, e);
        }
    }

    /**
     * Read the file from disk again, reusing the same configuration instance.
     */
    public void reload() {
        try {
            this.fileConfiguration.load(this.file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException("Could not reload " + fileName, e);
        }
    }
}
