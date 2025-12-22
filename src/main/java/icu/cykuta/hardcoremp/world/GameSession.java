package icu.cykuta.hardcoremp.world;

import icu.cykuta.hardcoremp.HardcoreMP;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

public class GameSession {
    private MultiverseWorld overworld;
    private MultiverseWorld nether;
    private MultiverseWorld end;
    private long createdTime;

    public GameSession(String overworld, long createdTime) {
        this.overworld = HardcoreMP.getMultiverseCore().getWorld(overworld).getOrNull();
        this.createdTime = createdTime == 0 ? System.currentTimeMillis() : createdTime;
    }

    public GameSession(long createdTime) {
        this.createdTime = createdTime == 0 ? System.currentTimeMillis() : createdTime;
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
}
