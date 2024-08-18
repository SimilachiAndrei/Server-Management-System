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
        try{
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            while (true) {
                // Read the command length
                int commandLength = inputStream.readInt();
                if (commandLength <= 0) {
                    break;  // End of stream or invalid length
                }

                // Read the command itself
                byte[] commandBytes = new byte[commandLength];
                inputStream.readFully(commandBytes);
                String command = new String(commandBytes);

                // Execute the command and capture output
                StringBuilder stringBuilder = getStringBuilder(command);

                // Send the output back to the client
                byte[] outputBytes = stringBuilder.toString().getBytes();
                outputStream.writeInt(outputBytes.length);
                System.out.println(stringBuilder.toString());
                outputStream.write(outputBytes);
            }

        }
        catch (IOException e) {
            System.out.println("Error handling command: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
