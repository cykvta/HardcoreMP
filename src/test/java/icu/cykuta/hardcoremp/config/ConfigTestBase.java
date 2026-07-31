package icu.cykuta.hardcoremp.config;

import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Binds Setting, LangManager and GameData to temporary files, so the config layer
 * can be exercised without a running server.
 */
public abstract class ConfigTestBase {

    @TempDir
    protected File folder;

    protected YamlFile writeConfig(String yaml) throws Exception {
        YamlFile file = write("config.yml", yaml);
        Setting.bind(file);
        return file;
    }

    protected YamlFile writeData(String yaml) throws Exception {
        YamlFile file = write("data.yml", yaml);
        GameData.bind(file);
        return file;
    }

    protected YamlFile writeLang(String yaml) throws Exception {
        YamlFile file = write("lang.yml", yaml);
        LangManager.bind(file);
        return file;
    }

    protected String read(String fileName) throws IOException {
        return new String(Files.readAllBytes(new File(folder, fileName).toPath()), StandardCharsets.UTF_8);
    }

    private YamlFile write(String fileName, String yaml) throws Exception {
        File file = new File(folder, fileName);
        Files.write(file.toPath(), yaml.getBytes(StandardCharsets.UTF_8));
        return YamlFile.open(file);
    }
}
