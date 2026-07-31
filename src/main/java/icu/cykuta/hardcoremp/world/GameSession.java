package icu.cykuta.hardcoremp.world;

import icu.cykuta.hardcoremp.config.GameData;
import icu.cykuta.hardcoremp.config.Setting;
import org.bukkit.World;

public class GameSession {
    private World overworld;
    private World nether;
    private World end;
    private long createdTime;
    private int lives;

    private GameSession(long createdTime, int lives) {
        this.createdTime = createdTime;
        this.lives = lives;
    }

    /**
     * Brand new session (freshly generated world): lives go back to the maximum.
     */
    public static GameSession createNew(long createdTime) {
        int maxLives = Setting.getMaxLives();
        GameSession session = new GameSession(
                createdTime == 0 ? System.currentTimeMillis() : createdTime,
                maxLives
        );
        GameData.setLives(maxLives);
        GameData.save();
        return session;
    }

    /**
     * Session restored from disk (server restart): keeps the stored lives and
     * creation time. It never resets them, since doing so would hand the players
     * free lives and hide how close the game is to a reset.
     */
    public static GameSession restore(long createdTime) {
        int lives = Math.max(0, Math.min(GameData.getLives(), Setting.getMaxLives()));
        return new GameSession(createdTime, lives);
    }

    public World getOverworld() {
        return overworld;
    }

    public World getNether() {
        return nether;
    }

    public World getEnd() {
        return end;
    }

    public long getCreatedTime() {
        return this.createdTime;
    }

    public GameSession setCreatedTime(long time) {
        this.createdTime = time;
        return this;
    }

    public GameSession setOverworld(World world) {
        this.overworld = world;
        return this;
    }

    public GameSession setNether(World world) {
        this.nether = world;
        return this;
    }

    public GameSession setEnd(World world) {
        this.end = world;
        return this;
    }

    public void removeLife() {
        this.lives = Math.max(0, this.lives - 1);
        GameData.setLives(this.lives);
        GameData.save();
    }

    public int getLives() {
        return this.lives;
    }
}
