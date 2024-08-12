package cockpit.childNode;

import java.net.Socket;

public class ThreadFactory {

    private ThreadFactory() {}

    public static Runnable createTask(ConnectionType type, Socket socket) {
        switch (type) {
            case COMMAND:
                return new CommandThread(socket);
            case CPU_USAGE:
                return new CpuUsageThread(socket);
            default:
                return null;
        }
    }
}
