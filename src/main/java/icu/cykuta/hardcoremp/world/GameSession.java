package icu.cykuta.hardcoremp.world;

import icu.cykuta.hardcoremp.config.Setting;
import org.bukkit.World;

public class GameSession {
    private World overworld;
    private World nether;
    private World end;
    private long createdTime;
    private int lives;

    public GameSession(long createdTime) {
        this.createdTime = createdTime == 0 ? System.currentTimeMillis() : createdTime;
        this.setSessionLives();
    }

    public GameSession() {
        this.setSessionLives();
    }

    private void setSessionLives() {
        Setting.setLives(Setting.getMaxLives());
        this.lives = Setting.getMaxLives();
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
        --this.lives;
        Setting.setLives(this.lives);
        Setting.saveConfig();
    }

    public int getLives() {
        return this.lives;
    }
}
