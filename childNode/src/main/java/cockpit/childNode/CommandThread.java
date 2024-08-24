package cockpit.childNode;

import java.io.*;
import java.net.Socket;

public class CommandThread implements Runnable {
    private Socket socket;
    private volatile boolean running = true;
    private Process process;

    public CommandThread(Socket socket) {
        this.socket = socket;
        System.out.println("CommandThread created");
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String command;
            while (running) {
                command = reader.readLine();

                if (command == null) {
                    System.out.println("Input stream closed, terminating CommandThread.");
                    break;
                } else if (command.trim().isEmpty()) {
                    System.out.println("Received empty command, ignoring.");
                    continue;
                }

                System.out.println("Command received: " + command);

                process = Runtime.getRuntime().exec(command);

                // Handle the output of the process in a separate thread
                Thread outputThread = new Thread(() -> {
                    try (BufferedReader processReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = processReader.readLine()) != null) {
                            writer.write(line);
                            writer.newLine();
                            writer.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                // Handle process error stream in a separate thread
                Thread errorThread = new Thread(() -> {
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String errorLine;
                        while ((errorLine = errorReader.readLine()) != null) {
                            writer.write(errorLine);
                            writer.newLine();
                            writer.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                // Handle input to the process in a separate thread
                Thread inputThread = new Thread(() -> {
                    try (BufferedWriter processWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                        String input;
                        while (running && (input = reader.readLine()) != null) {
                            processWriter.write(input);
                            processWriter.newLine();
                            processWriter.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                // Start the threads
                outputThread.start();
                errorThread.start();
                inputThread.start();

                // Wait for the process to finish
                process.waitFor();

                outputThread.join();
                errorThread.join();
                inputThread.join();

                writer.write("\nCommand execution finished.\n");
                writer.flush();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        if (process != null && process.isAlive()) {
            process.destroy();
        }
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