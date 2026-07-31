package icu.cykuta.hardcoremp.listener;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.config.ConfigTestBase;
import icu.cykuta.hardcoremp.config.GameData;
import icu.cykuta.hardcoremp.world.GameSession;
import icu.cykuta.hardcoremp.world.WorldManager;
import icu.cykuta.hardcoremp.world.WorldStatus;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class PlayerDeathTest extends ConfigTestBase {

    private MockedStatic<Bukkit> bukkit;
    private MockedStatic<HardcoreMP> hardcoreMP;

    private WorldManager worldManager;
    private GameSession session;
    private World overworld;
    private World lobby;
    private PlayerDeath listener;

    @BeforeEach
    void setUp() throws Exception {
        writeConfig("setting:\n  max-lives: 3\n  user-bypass-list:\n    - admin\n");
        writeData("");
        writeLang("death-title: \"&cDeath\"\n"
                + "death-subtitle: \"&7{player} died\"\n"
                + "lives-left: \"&c{lives}/{max-lives}\"\n"
                + "bypass: \"&aBypassed\"\n");

        overworld = mock(World.class);
        lobby = mock(World.class);

        session = GameSession.createNew(1_000L).setOverworld(overworld);

        worldManager = mock(WorldManager.class);
        when(worldManager.getGameSession()).thenReturn(session);
        when(worldManager.getStatus()).thenReturn(WorldStatus.READY);

        bukkit = mockStatic(Bukkit.class);
        bukkit.when(Bukkit::getOnlinePlayers).thenReturn(Collections.emptyList());
        bukkit.when(Bukkit::getScheduler).thenReturn(mock(BukkitScheduler.class));

        hardcoreMP = mockStatic(HardcoreMP.class);
        hardcoreMP.when(HardcoreMP::getWorldManager).thenReturn(worldManager);

        listener = new PlayerDeath();
    }

    @AfterEach
    void tearDown() {
        bukkit.close();
        hardcoreMP.close();
    }

    @Test
    @DisplayName("three lives means the third death resets the world")
    void thirdDeathResetsTheWorld() {
        listener.onPlayerDeath(deathIn(overworld, "steve"));
        assertEquals(2, session.getLives());

        listener.onPlayerDeath(deathIn(overworld, "steve"));
        assertEquals(1, session.getLives());
        verify(worldManager, never()).regenGameWorld();

        listener.onPlayerDeath(deathIn(overworld, "steve"));
        assertEquals(0, session.getLives());
        // The reset is scheduled one tick later so the player leaves the death screen
        verify(Bukkit.getScheduler()).runTaskLater(any(), any(Runnable.class), anyLong());
    }

    @Test
    @DisplayName("dying in the lobby does not cost a life")
    void lobbyDeathIsFree() {
        listener.onPlayerDeath(deathIn(lobby, "steve"));

        assertEquals(3, session.getLives());
        verify(Bukkit.getScheduler(), never()).runTaskLater(any(), any(Runnable.class), anyLong());
    }

    @Test
    @DisplayName("a player in the bypass list does not cost a life")
    void bypassedPlayerKeepsTheLives() {
        listener.onPlayerDeath(deathIn(overworld, "admin"));

        assertEquals(3, session.getLives());
    }

    @Test
    @DisplayName("a death while the world is being generated is ignored")
    void deathWithoutASessionIsIgnored() {
        when(worldManager.getGameSession()).thenReturn(null);

        // Used to throw a NullPointerException inside the event handler
        assertDoesNotThrow(() -> listener.onPlayerDeath(deathIn(overworld, "steve")));
        assertEquals(3, session.getLives());
    }

    @Test
    @DisplayName("a death during a reset does not consume another life")
    void deathWhileRegeneratingIsIgnored() {
        when(worldManager.getStatus()).thenReturn(WorldStatus.REGENERATING);

        listener.onPlayerDeath(deathIn(overworld, "steve"));

        assertEquals(3, session.getLives());
        assertEquals(3, GameData.getLives());
    }

    private PlayerDeathEvent deathIn(World world, String playerName) {
        Player player = mock(Player.class);
        when(player.getWorld()).thenReturn(world);
        when(player.getName()).thenReturn(playerName);
        return new PlayerDeathEvent(player, new ArrayList<>(), 0, null);
    }
}
