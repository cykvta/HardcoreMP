package icu.cykuta.hardcoremp.world;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.config.Setting;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

public class GameSession {
    private MultiverseWorld overworld;
    private MultiverseWorld nether;
    private MultiverseWorld end;
    private long createdTime;
    private int lives;

    public GameSession(String overworld, long createdTime) {
        this.overworld = HardcoreMP.getMultiverseCore().getWorld(overworld).getOrNull();
        this.createdTime = createdTime == 0 ? System.currentTimeMillis() : createdTime;
        this.setSessionLives();
    }

    public GameSession(long createdTime) {
        this.createdTime = createdTime == 0 ? System.currentTimeMillis() : createdTime;
        this.setSessionLives();
    }

    private void setSessionLives() {
        Setting.setLives(Setting.getMaxLives());
        this.lives = Setting.getMaxLives();
    }

    public MultiverseWorld getOverworld() {
        return overworld;
    }

    public MultiverseWorld getNether() {
        return nether;
    }

    public MultiverseWorld getEnd() {
        return end;
    }

    public long getCreatedTime() {
        return this.createdTime;
    }

    public void setCreatedTime(long time) {
        this.createdTime = time;
    }

    public GameSession setOverworld(MultiverseWorld world) {
        this.overworld = world;
        return this;
    }

    public GameSession setNether(MultiverseWorld world) {
        this.nether = world;
        return this;
    }

    public GameSession setEnd(MultiverseWorld world) {
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
