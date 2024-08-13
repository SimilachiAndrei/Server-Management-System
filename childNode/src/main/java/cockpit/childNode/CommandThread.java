package cockpit.childNode;

import java.io.*;
import java.net.Socket;

public class CommandThread implements Runnable {
    private Socket socket;

    public CommandThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(),true)) {
            while (!socket.isClosed()) {
                String command = input.readLine().trim();
                System.out.println(command + "\n");
                try {
                    StringBuilder stringBuilder = getStringBuilder(command);
                    System.out.println(stringBuilder.toString());
                    output.println(stringBuilder.toString());
                } catch (IOException | InterruptedException exception) {
                    System.out.println(exception.getMessage());
                    output.println("An exception has occured: " + exception.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling command: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private static StringBuilder getStringBuilder(String command) throws InterruptedException, IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader processOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = processOutput.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        int exitCode = process.waitFor();
        stringBuilder.append("Process has finished with exit code: ").append(exitCode);
        return stringBuilder;
    }
}
