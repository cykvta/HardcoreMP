package icu.cykuta.hardcoremp.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingTest extends ConfigTestBase {

    private static final String FULL_CONFIG =
            "setting:\n" +
            "  lobby-world: \"lobby\"\n" +
            "  remove-old-worlds: false\n" +
            "  motd: true\n" +
            "  offline-player-inventory-clear: true\n" +
            "  max-lives: 5\n" +
            "  user-bypass-list:\n" +
            "    - cykuta\n";

    @Test
    @DisplayName("reads the values of a complete config")
    void readsValues() throws Exception {
        writeConfig(FULL_CONFIG);

        assertEquals("lobby", Setting.getLobbyWorldName());
        assertEquals(5, Setting.getMaxLives());
        assertFalse(Setting.removeOldWorlds());
        assertTrue(Setting.isMotdEnabled());
        assertTrue(Setting.isOfflinePlayerInventoryClearEnabled());
        assertTrue(Setting.isPlayerInBypassList("cykuta"));
        assertFalse(Setting.isPlayerInBypassList("someone-else"));
    }

    @Test
    @DisplayName("max-lives falls back to 3 instead of 0 when the key is missing")
    void maxLivesFallsBack() throws Exception {
        // A config written by an older version has no max-lives. Returning 0 made
        // the very first death reset the world.
        writeConfig("setting:\n  lobby-world: \"world\"\n");

        assertEquals(3, Setting.getMaxLives());
    }

    @Test
    @DisplayName("a negative max-lives is clamped to 0")
    void negativeMaxLivesIsClamped() throws Exception {
        writeConfig("setting:\n  max-lives: -4\n");

        assertEquals(0, Setting.getMaxLives());
    }

    @Test
    @DisplayName("a missing bypass list is not an error")
    void missingBypassList() throws Exception {
        // getList() returns null here, and the raw call used to throw a NPE
        // inside the death handler, cancelling the whole reset.
        writeConfig("setting:\n  motd: true\n");

        assertFalse(Setting.isPlayerInBypassList("cykuta"));
    }

    @Test
    @DisplayName("lobby-world falls back to 'world' when missing or blank")
    void lobbyWorldFallsBack() throws Exception {
        writeConfig("setting:\n  motd: true\n");
        assertEquals("world", Setting.getLobbyWorldName());

        writeConfig("setting:\n  lobby-world: \"   \"\n");
        assertEquals("world", Setting.getLobbyWorldName());
    }
}
