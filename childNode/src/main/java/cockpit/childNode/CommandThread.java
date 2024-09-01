package cockpit.childNode;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

public class CommandThread implements Runnable {
    private Socket socket;
    private volatile boolean running = true;
    private Process currentProcess;
    private ExecutorService executor;

    public CommandThread(Socket socket) {
        this.socket = socket;
        this.executor = Executors.newCachedThreadPool();
        System.out.println("CommandThread created");
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            while (running) {
                String command = reader.readLine();

                if (command == null) {
                    System.out.println("Input stream closed, terminating CommandThread.");
                    break;
                } else if (command.trim().isEmpty()) {
                    System.out.println("Received empty command, ignoring.");
                    continue;
                }

                System.out.println("Command received: " + command);

                // Terminate the previous process if it's still running
                if (currentProcess != null && currentProcess.isAlive()) {
                    currentProcess.destroy();
                    currentProcess.waitFor();
                }

                currentProcess = Runtime.getRuntime().exec(command);

                Future<?> outputFuture = executor.submit(() -> handleStream(currentProcess.getInputStream(), writer));
                Future<?> errorFuture = executor.submit(() -> handleStream(currentProcess.getErrorStream(), writer));

                // Handle input in the main thread
                try (BufferedWriter processWriter = new BufferedWriter(new OutputStreamWriter(currentProcess.getOutputStream()))) {
                    String input;
                    while (currentProcess.isAlive()) {
                        if (reader.ready()) {
                            input = reader.readLine();
                            if ("EXIT_COMMAND".equals(input)) {
                                break;
                            }
                            processWriter.write(input);
                            processWriter.newLine();
                            processWriter.flush();
                        }
                        Thread.sleep(100);  // Small delay to prevent busy waiting
                    }
                }

                // Wait for the process to finish
                currentProcess.waitFor();

                // Wait for output and error streams to be fully processed
                outputFuture.get();
                errorFuture.get();

                writer.write("Command completed.\n");
                writer.flush();
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void handleStream(InputStream inputStream, BufferedWriter writer) {
        try (BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = streamReader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanup() {
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroy();
        }
        executor.shutdownNow();
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Socket closed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("CommandThread terminated");
    }

    public void stop() {
        running = false;
        cleanup();
    }
}