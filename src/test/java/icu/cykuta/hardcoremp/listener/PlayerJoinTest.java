package icu.cykuta.hardcoremp.listener;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.config.ConfigTestBase;
import icu.cykuta.hardcoremp.world.GameSession;
import icu.cykuta.hardcoremp.world.WorldManager;
import icu.cykuta.hardcoremp.world.WorldStatus;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlayerJoinTest extends ConfigTestBase {

    private MockedStatic<Bukkit> bukkit;
    private MockedStatic<HardcoreMP> hardcoreMP;

    private WorldManager worldManager;
    private World gameWorld;
    private World limbo;
    private PlayerJoin listener;

    private Player player;
    private PlayerInventory inventory;
    private Inventory enderChest;

    @BeforeEach
    void setUp() throws Exception {
        writeData("");
        writeLang("world-generating: \"&eWait\"\n");

        gameWorld = mock(World.class);
        limbo = mock(World.class);

        worldManager = mock(WorldManager.class);
        when(worldManager.getGameSession()).thenReturn(GameSession.createNew(1L).setOverworld(gameWorld));
        when(worldManager.getStatus()).thenReturn(WorldStatus.READY);
        when(worldManager.getLobbyWorldName()).thenReturn("lobby");
        when(worldManager.getLimboWorld()).thenReturn(limbo);
        when(worldManager.getLimboSpawn()).thenReturn(mock(Location.class));

        bukkit = mockStatic(Bukkit.class);
        bukkit.when(Bukkit::getScheduler).thenReturn(mock(BukkitScheduler.class));

        hardcoreMP = mockStatic(HardcoreMP.class);
        hardcoreMP.when(HardcoreMP::getWorldManager).thenReturn(worldManager);

        inventory = mock(PlayerInventory.class);
        enderChest = mock(Inventory.class);
        player = mock(Player.class);
        when(player.getWorld()).thenReturn(gameWorld);
        when(player.getInventory()).thenReturn(inventory);
        when(player.getEnderChest()).thenReturn(enderChest);
        when(player.getActivePotionEffects()).thenReturn(Collections.emptyList());
        when(player.getMaxHealth()).thenReturn(20.0);
        when(player.isOnline()).thenReturn(true);
        when(gameWorld.getName()).thenReturn("hcmp_game");

        listener = new PlayerJoin();
    }

    @AfterEach
    void tearDown() {
        bukkit.close();
        hardcoreMP.close();
    }

    @Test
    @DisplayName("a returning player keeps his inventory when no reset happened")
    void keepsInventoryWithoutAReset() throws Exception {
        writeConfig("setting:\n  offline-player-inventory-clear: true\n  max-lives: 3\n");
        when(worldManager.playerMissedReset(player)).thenReturn(false);

        listener.onPlayerJoin(new PlayerJoinEvent(player, ""));

        // This is the restart bug: every player used to be wiped on join
        verify(inventory, never()).clear();
        verify(enderChest, never()).clear();
        verify(worldManager).markPlayerReset(player);
    }

    @Test
    @DisplayName("a player that missed a reset is cleared when the option is on")
    void clearsInventoryOfPlayersThatMissedAReset() throws Exception {
        writeConfig("setting:\n  offline-player-inventory-clear: true\n  max-lives: 3\n");
        when(worldManager.playerMissedReset(player)).thenReturn(true);

        listener.onPlayerJoin(new PlayerJoinEvent(player, ""));

        verify(inventory).clear();
        verify(enderChest).clear();
        verify(worldManager).markPlayerReset(player);
    }

    @Test
    @DisplayName("the clear option is honoured when turned off")
    void respectsTheDisabledOption() throws Exception {
        writeConfig("setting:\n  offline-player-inventory-clear: false\n  max-lives: 3\n");
        when(worldManager.playerMissedReset(player)).thenReturn(true);

        listener.onPlayerJoin(new PlayerJoinEvent(player, ""));

        // The reset branch used to clear the inventory whatever the config said
        verify(inventory, never()).clear();
        verify(worldManager).markPlayerReset(player);
    }

    @Test
    @DisplayName("a player joining while the world is generating waits in the limbo")
    void waitsInLimboWhileGenerating() throws Exception {
        writeConfig("setting:\n  offline-player-inventory-clear: true\n  max-lives: 3\n");
        when(worldManager.getStatus()).thenReturn(WorldStatus.GENERATING);
        when(worldManager.playerMissedReset(player)).thenReturn(false);

        listener.onPlayerJoin(new PlayerJoinEvent(player, ""));

        verify(player).setGameMode(GameMode.SPECTATOR);
        verify(player).teleport(any(Location.class));
        verify(worldManager).registerWorldReadyCallback(any());
        // Marked only once the world is ready, not before
        verify(worldManager, never()).markPlayerReset(player);
    }
}
