package icu.cykuta.hardcoremp.config;

public enum Setting {
    USER_BYPASS_LIST("user-bypass-list"),
    GAME_WORLD("game-world"),
    LOBBY_WORLD("lobby-world"),
    MOTD("motd");

    private final String path;

    Setting(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "setting." + this.path;
    }
}
