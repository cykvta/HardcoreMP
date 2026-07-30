package icu.cykuta.hardcoremp.world;

import java.util.function.Supplier;

public class WorldLoadException<X> implements Supplier<X> {

    public WorldLoadException(String worldname) {
        super();
    }

    @Override
    public X get() {
        return null;
    }
}

