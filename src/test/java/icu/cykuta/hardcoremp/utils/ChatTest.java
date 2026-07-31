package icu.cykuta.hardcoremp.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatTest {

    @Test
    @DisplayName("translates the colour codes")
    void translatesColourCodes() {
        assertEquals("§aReady", Chat.color("&aReady"));
        assertEquals("§c§lBold red", Chat.color("&c&lBold red"));
    }

    @Test
    @DisplayName("leaves an ampersand that is not a colour code alone")
    void keepsPlainAmpersands() {
        // The old implementation replaced every '&', so a name like "Tom & Jerry"
        // reached the chat as "Tom § Jerry"
        assertEquals("Tom & Jerry", Chat.color("Tom & Jerry"));
        assertEquals("a & b", Chat.color("a & b"));
    }

    @Test
    @DisplayName("a null message does not blow up")
    void nullIsSafe() {
        assertEquals("", Chat.color(null));
    }
}
