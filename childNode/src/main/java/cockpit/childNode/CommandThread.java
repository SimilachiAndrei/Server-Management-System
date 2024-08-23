package cockpit.childNode;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class CommandThread implements Runnable {
    private Socket socket;
    private volatile boolean running = true;

    public CommandThread(Socket socket) {
        this.socket = socket;
        System.out.println("CommandThread created");
    }


    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String command;
            while ((command = reader.readLine()) != null) {
                System.out.println("Command received: " + command);
                Process process = Runtime.getRuntime().exec(command);

                BufferedReader processReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedWriter processWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

                new Thread(() -> {
                    try {
                        String line;
                        while ((line = processReader.readLine()) != null) {
                            writer.write(line);
                            writer.newLine();
                            writer.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    processWriter.write(inputLine);
                    processWriter.flush();
                }

                process.waitFor();  // Wait for the process to finish
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}