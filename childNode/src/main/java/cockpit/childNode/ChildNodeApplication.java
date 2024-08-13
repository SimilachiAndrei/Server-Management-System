package cockpit.childNode;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static cockpit.childNode.ConnectionType.getNextConnectionType;

public class ChildNodeApplication {
    private final String motherAddress = "127.0.0.1";
    private final int motherPort = 4000;
    private final int port = 4001;
    private boolean running = true;
    private ExecutorService threadPool = Executors.newFixedThreadPool(2); // Pool size should match the number of concurrent tasks

    private ConnectionType currentConnectionType = ConnectionType.COMMAND;

    public ChildNodeApplication() {
        startServer();
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (running) {
                // Accept and handle connections based on type
                System.out.println("Waiting for a connection !");
                Socket socket = serverSocket.accept();
                String incomingAddress = socket.getInetAddress().getHostAddress();
                int incomingPort = socket.getPort();

                if (!validateConnection(incomingAddress)) {
                    System.out.println("Unauthorized connection attempt from " + incomingAddress + ":" + incomingPort);
                    socket.close();
                    continue;
                }

                Runnable task = ThreadFactory.createTask(currentConnectionType, socket);
                if (task != null) {
                    threadPool.submit(task);
                    currentConnectionType = getNextConnectionType(currentConnectionType);
                    if (currentConnectionType == ConnectionType.NONE) {
                        currentConnectionType = ConnectionType.COMMAND;
                        waitForThreadsToComplete();
                    }
                } else {
                    System.out.println("No task created for connection type: " + currentConnectionType);
                    socket.close();
                }
            }
        } catch (IOException exception) {
            System.out.println("Caught error: " + exception.getMessage());
        } finally {
            waitForThreadsToComplete();
        }
    }

    private boolean validateConnection(String incomingAddress) {
        return motherAddress.equals(incomingAddress);
    }

    private void waitForThreadsToComplete() {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Thread pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        new ChildNodeApplication();
    }
}
