package cockpit.childNode;

public enum ConnectionType {
    COMMAND, CPU_USAGE, NONE;

    public static ConnectionType getNextConnectionType(ConnectionType currentType) {
        switch (currentType) {
            case COMMAND:
                return CPU_USAGE;
            case CPU_USAGE:
                return COMMAND;
            default:
                return NONE;
        }
    }
}
