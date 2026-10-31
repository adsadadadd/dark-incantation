package me.x1no4ekk.core.top;

/**
 * Kinds of leaderboards.
 */
public enum TopType {
    KILLS("killtop"),
    DEATHS("deathtop"),
    BALANCE("baltop");

    private final String command;

    TopType(String command) {
        this.command = command;
    }

    /**
     * Command name; also the permission suffix ({@code core.<command>}) and the messages key.
     */
    public String command() {
        return command;
    }
}
