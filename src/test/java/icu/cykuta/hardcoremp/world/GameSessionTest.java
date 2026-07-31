package icu.cykuta.hardcoremp.world;

import icu.cykuta.hardcoremp.config.ConfigTestBase;
import icu.cykuta.hardcoremp.config.GameData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest extends ConfigTestBase {

    @Test
    @DisplayName("a new session starts with the maximum lives and persists them")
    void newSessionResetsLives() throws Exception {
        writeConfig("setting:\n  max-lives: 3\n");
        writeData("data:\n  current-lives: 1\n");

        GameSession session = GameSession.createNew(1_000L);

        assertEquals(3, session.getLives());
        assertEquals(1_000L, session.getCreatedTime());
        assertEquals(3, GameData.getLives());
    }

    @Test
    @DisplayName("a new session without a timestamp uses the current time")
    void newSessionWithoutTimestamp() throws Exception {
        writeConfig("setting:\n  max-lives: 3\n");
        writeData("");

        long before = System.currentTimeMillis();
        GameSession session = GameSession.createNew(0);

        assertTrue(session.getCreatedTime() >= before);
    }

    @Test
    @DisplayName("a restored session keeps the stored lives across a restart")
    void restoreKeepsLives() throws Exception {
        writeConfig("setting:\n  max-lives: 3\n");
        writeData("data:\n  current-lives: 1\n");

        GameSession session = GameSession.restore(555L);

        // Rebuilding the session used to hand the players their lives back
        assertEquals(1, session.getLives());
        assertEquals(555L, session.getCreatedTime());
    }

    @Test
    @DisplayName("a restored session clamps lives to the configured maximum")
    void restoreClampsLives() throws Exception {
        writeConfig("setting:\n  max-lives: 2\n");
        writeData("data:\n  current-lives: 99\n");
        assertEquals(2, GameSession.restore(0).getLives());

        writeData("data:\n  current-lives: -5\n");
        assertEquals(0, GameSession.restore(0).getLives());
    }

    @Test
    @DisplayName("losing a life persists it and never goes below zero")
    void removeLifeFloorsAtZero() throws Exception {
        writeConfig("setting:\n  max-lives: 2\n");
        writeData("");

        GameSession session = GameSession.createNew(1L);
        session.removeLife();
        assertEquals(1, session.getLives());
        assertEquals(1, GameData.getLives());

        session.removeLife();
        assertEquals(0, session.getLives());

        session.removeLife();
        assertEquals(0, session.getLives());
        assertEquals(0, GameData.getLives());
    }
}
