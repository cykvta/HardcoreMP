package icu.cykuta.hardcoremp.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameDataTest extends ConfigTestBase {

    @Test
    @DisplayName("stores and reloads the game state")
    void storesState() throws Exception {
        writeConfig("setting:\n  max-lives: 3\n");
        YamlFile data = writeData("");

        GameData.setCreateTime(1_700_000_000_000L);
        GameData.setResetId(7);
        GameData.setLives(2);
        GameData.save();

        data.reload();
        assertEquals(1_700_000_000_000L, GameData.getCreateTime());
        assertEquals(7, GameData.getResetId());
        assertEquals(2, GameData.getLives());
    }

    @Test
    @DisplayName("an empty data file does not mean 'no lives left'")
    void livesFallBackToMax() throws Exception {
        writeConfig("setting:\n  max-lives: 4\n");
        writeData("");

        assertEquals(4, GameData.getLives());
        assertEquals(0, GameData.getResetId());
        assertEquals(0L, GameData.getCreateTime());
    }

    @Test
    @DisplayName("zero lives is honoured and not confused with a missing key")
    void zeroLivesIsKept() throws Exception {
        writeConfig("setting:\n  max-lives: 3\n");
        writeData("data:\n  current-lives: 0\n");

        assertEquals(0, GameData.getLives());
    }

    @Test
    @DisplayName("migrates the legacy keys out of config.yml")
    void migratesLegacyKeys() throws Exception {
        YamlFile config = writeConfig(
                "setting:\n" +
                "  lobby-world: \"world\"\n" +
                "  max-lives: 3\n" +
                "  current-lives: 1\n" +
                "  create-time: 1234\n" +
                "  reset-id: 9\n");
        writeData("");

        assertTrue(GameData.migrateFromConfig(config));

        assertEquals(1234L, GameData.getCreateTime());
        assertEquals(9, GameData.getResetId());
        assertEquals(1, GameData.getLives());

        // The legacy keys are gone, the real settings stay
        assertFalse(config.getFileConfiguration().contains("setting.create-time"));
        assertFalse(config.getFileConfiguration().contains("setting.reset-id"));
        assertFalse(config.getFileConfiguration().contains("setting.current-lives"));
        assertEquals("world", Setting.getLobbyWorldName());
        assertEquals(3, Setting.getMaxLives());

        // And they survive a round trip to disk
        config.reload();
        assertFalse(config.getFileConfiguration().contains("setting.reset-id"));
        assertTrue(read("data.yml").contains("reset-id"));
    }

    @Test
    @DisplayName("migration never overwrites a value already present in data.yml")
    void migrationDoesNotOverwrite() throws Exception {
        YamlFile config = writeConfig("setting:\n  reset-id: 9\n  current-lives: 1\n");
        writeData("data:\n  reset-id: 42\n");

        assertTrue(GameData.migrateFromConfig(config));

        assertEquals(42, GameData.getResetId());
        assertEquals(1, GameData.getLives());
    }

    @Test
    @DisplayName("nothing to migrate on a clean install")
    void nothingToMigrate() throws Exception {
        YamlFile config = writeConfig("setting:\n  max-lives: 3\n");
        writeData("");

        assertFalse(GameData.migrateFromConfig(config));
    }
}
