package cockpit.childNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class CpuUsageThread implements Runnable {
    private Socket socket;

    public CpuUsageThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (InputStream input = socket.getInputStream()) {
            // Handle CPU usage data communication here
        } catch (IOException e) {
            System.out.println("Error handling CPU usage: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}
