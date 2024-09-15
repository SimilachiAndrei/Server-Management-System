package cockpit.childNode;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class CommandThread implements Runnable {
    private final Socket socket;
    private final ExecutorService executor;

    public CommandThread(Socket socket) {
        this.socket = socket;
        this.executor = Executors.newCachedThreadPool();
        System.out.println("CommandThread created");
    }

    @Override
    public void run() {
        try (BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream())) {

            Map<String, String> environment = new HashMap<>(System.getenv());
            PtyProcess shellProcess = new PtyProcessBuilder(new String[]{"bash", "-i"})
                    .setEnvironment(environment)
                    .setInitialRows(24)
                    .setInitialColumns(80)
                    .start();

            Future<?> outputThread = executor.submit(() -> handleInputStream(outputStream, shellProcess.getInputStream()));
            Future<?> errorThread = executor.submit(() -> handleInputStream(outputStream, shellProcess.getErrorStream()));

            InputStream socketInputStream = socket.getInputStream();
            OutputStream processOutputStream = shellProcess.getOutputStream();

            byte[] buffer = new byte[4096];
            int bytesRead;

            while (!socket.isClosed() && (bytesRead = socketInputStream.read(buffer)) != -1) {
                processOutputStream.write(buffer, 0, bytesRead);
                processOutputStream.flush();
            }

            // Socket closed, clean up
            shellProcess.destroy();
            outputThread.cancel(true);
            errorThread.cancel(true);
            executor.shutdownNow();
            System.out.println("CommandThread: Socket closed, resources cleaned up.");

        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (!executor.isShutdown()) {
                    executor.shutdownNow();
                }
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleInputStream(BufferedOutputStream writer, InputStream inputStream) {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while (!Thread.currentThread().isInterrupted() && (bytesRead = bufferedInputStream.read(buffer)) != -1) {
                writer.write(buffer, 0, bytesRead);
                writer.flush();
                System.out.println("Wrote to socket : " + new String(buffer, 0, bytesRead));
            }
        } catch (IOException exception) {
            if (!Thread.currentThread().isInterrupted()) {
                exception.printStackTrace();
            } else {
                System.out.println("Thread interrupted, stopping input stream handling.");
            }
        }
    }
}