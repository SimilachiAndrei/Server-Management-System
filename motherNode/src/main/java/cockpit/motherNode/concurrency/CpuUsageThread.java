package cockpit.motherNode.concurrency;

import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

@Setter
@Getter

public class CpuUsageThread implements Runnable {

    private Socket socket;
    private String address;
    private Integer port;
    private boolean success = false;
    private final SimpMessagingTemplate messagingTemplate;

    public CpuUsageThread(String address, Integer port, SimpMessagingTemplate messagingTemplate) {
        this.address = address;
        this.port = port;
        this.messagingTemplate = messagingTemplate;
    }


    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            success = true;
            System.out.println("After connection : " + success);
            // Continuously receive CPU usage data and forward to the frontend
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            while (true) {
                int messageLength = inputStream.readInt();
                byte[] data = new byte[messageLength];
                inputStream.read(data);
                String response = new String(data);
                messagingTemplate.convertAndSend("/topic/cpuUsage", response);
            }
        } catch (IOException e) {
            success = false;
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


