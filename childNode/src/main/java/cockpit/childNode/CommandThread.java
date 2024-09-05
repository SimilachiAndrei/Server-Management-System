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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream())) {
            String command;
            while (running) {
                command = reader.readLine();
                System.out.println("Command received : " + command);

                if (command.isEmpty()) break;

                Map<String, String> environment = new HashMap<>(System.getenv());
                String[] commandArray = new String[]{"bash", "-c", command};

                PtyProcessBuilder processBuilder = new PtyProcessBuilder(commandArray);
                processBuilder.setEnvironment(environment);

                PtyProcess process = processBuilder.start();

                Future<?> outputThread = executor.submit(() -> handleInputStream(outputStream, process.getInputStream()));
                Future<?> errorThread = executor.submit(() -> handleInputStream(outputStream, process.getErrorStream()));

                PrintWriter processWriter = new PrintWriter(process.getOutputStream(), true);
                String line;
                while (process.isAlive()) {
                    if (reader.ready() && (line = reader.readLine()) != null)
                    {
                        processWriter.println(line);
                        processWriter.flush();
                    }
                }

                outputThread.get();
                errorThread.get();
                process.waitFor();
            }
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
                System.out.println("Wrote to socket : " + (new String(buffer)));
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}