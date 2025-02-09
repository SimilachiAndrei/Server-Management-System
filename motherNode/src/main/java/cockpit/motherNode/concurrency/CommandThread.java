package cockpit.motherNode.concurrency;

import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

@Setter
@Getter

public class CommandThread implements Runnable {

    private Socket socket;
    private String address;
    private Integer port;
    private String name;
    private boolean success = false;
    private final SimpMessagingTemplate messagingTemplate;

    public CommandThread(String address, Integer port, String name, SimpMessagingTemplate messagingTemplate) {
        this.address = address;
        this.port = port;
        this.name = name;
        this.messagingTemplate = messagingTemplate;
    }


    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            success = true;
            System.out.println("Command thread socket : " + success);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            char[] buffer = new char[1024];
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) != -1) {
                String output = new String(buffer, 0, bytesRead);
                System.out.println("Command thread : " + output);  // Debugging: Print to console
                final String path = "/topic/terminalOutput/".concat(this.name);
                messagingTemplate.convertAndSend(name, output);
                System.out.println("Message should be sent");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            // Handle the error
        }
    }
}


