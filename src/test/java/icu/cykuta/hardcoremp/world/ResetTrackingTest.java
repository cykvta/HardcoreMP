package icu.cykuta.hardcoremp.world;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The rule that decides whether a joining player has his inventory cleared.
 */
class ResetTrackingTest {

    @Test
    @DisplayName("a player marked before the last reset missed it")
    void olderMarkMissedTheReset() {
        assertTrue(WorldManager.missedReset(2, 3));
        assertTrue(WorldManager.missedReset(0, 1));
    }

    @Test
    @DisplayName("a player marked with the current reset did not miss it")
    void currentMarkIsFine() {
        assertFalse(WorldManager.missedReset(3, 3));
    }

    @Test
    @DisplayName("an unmarked player is never treated as having missed a reset")
    void unmarkedPlayerIsNeverWiped() {
        // First join, or an upgrade from a version without the mark. Wiping here
        // emptied the inventory of players who had done nothing wrong.
        assertFalse(WorldManager.missedReset(null, 7));
    }

    @Test
    @DisplayName("a mark ahead of the server is not a missed reset")
    void newerMarkIsNotAMiss() {
        // Happens when data.yml is restored from an older backup
        assertFalse(WorldManager.missedReset(9, 4));
    }
}
