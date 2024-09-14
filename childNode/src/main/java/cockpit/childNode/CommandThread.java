package cockpit.childNode;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class CommandThread implements Runnable {
    private Socket socket;
    private volatile boolean running = true;
    private ExecutorService executor;

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

            shellProcess.destroy();
            outputThread.get();
            errorThread.get();
        } catch (IOException | InterruptedException | ExecutionException exception) {
            exception.printStackTrace();
        }
    }

    private static void handleInputStream(BufferedOutputStream writer, InputStream inputStream) {
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                writer.write(buffer, 0, bytesRead);
                writer.flush();
                System.out.println("Wrote to socket : " + new String(buffer, 0, bytesRead));
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}