package icu.cykuta.hardcoremp.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LangManagerTest extends ConfigTestBase {

    @Test
    @DisplayName("reads and colours a message, including nested keys")
    void readsMessages() throws Exception {
        writeLang(
                "death-title: \"&cDeath\"\n" +
                "motd:\n" +
                "  ready: \"&aThe game world is ready\"\n");

        assertEquals("§cDeath", LangManager.getLang("death-title"));
        assertEquals("§aThe game world is ready", LangManager.getLang("motd.ready"));
    }

    @Test
    @DisplayName("a missing key reports itself instead of returning null")
    void missingKey() throws Exception {
        writeLang("death-title: \"&cDeath\"\n");

        assertEquals("nope is not found in lang.yml.", LangManager.getLang("nope"));
    }

    @Test
    @DisplayName("the shipped lang.yml holds every key the plugin asks for")
    void shippedFileIsComplete() throws Exception {
        YamlFile lang = YamlFile.open(new java.io.File("src/main/resources/lang.yml"));
        LangManager.bind(lang);

        String[] keys = {
                "bypass", "death-title", "death-subtitle", "kick", "world-not-ready",
                "world-generating", "reload", "lives-left", "unknown-command",
                "motd.ready", "motd.not-ready", "motd.unknown",
                "help.separator", "help.title", "help.help", "help.reload", "help.info",
                "info.only-players", "info.no-session", "info.separator", "info.title",
                "info.world", "info.seed", "info.difficulty", "info.location", "info.spawn",
                "info.time", "info.weather", "info.world-type", "info.lives",
                "info.time-day", "info.time-sunset", "info.time-night", "info.time-dawn",
                "info.weather-clear", "info.weather-rain", "info.weather-thunder",
                "info.type-overworld", "info.type-nether", "info.type-end",
                "info.type-lobby", "info.type-unknown"
        };

        for (String key : keys) {
            assertNotNull(lang.getFileConfiguration().getString(key), "missing key in lang.yml: " + key);
        }
    }

    @Test
    @DisplayName("the shipped data.yml carries no values, so an upgrade can migrate")
    void shippedDataFileIsEmpty() throws Exception {
        YamlFile data = YamlFile.open(new java.io.File("src/main/resources/data.yml"));

        // If the defaults were present, migrateFromConfig() would never copy the
        // values an older install kept inside config.yml.
        assertFalse(data.getFileConfiguration().contains("data.create-time"));
        assertFalse(data.getFileConfiguration().contains("data.reset-id"));
        assertFalse(data.getFileConfiguration().contains("data.current-lives"));
    }

    @Test
    @DisplayName("the shipped config.yml no longer carries runtime state")
    void shippedConfigHasNoState() throws Exception {
        YamlFile config = YamlFile.open(new java.io.File("src/main/resources/config.yml"));
        Setting.bind(config);

        assertFalse(config.getFileConfiguration().contains("setting.create-time"));
        assertFalse(config.getFileConfiguration().contains("setting.reset-id"));
        assertFalse(config.getFileConfiguration().contains("setting.current-lives"));
        assertFalse(config.getFileConfiguration().contains("lang"));

        assertEquals("world", Setting.getLobbyWorldName());
        assertEquals(3, Setting.getMaxLives());
    }
}
