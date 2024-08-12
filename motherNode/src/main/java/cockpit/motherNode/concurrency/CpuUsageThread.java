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

            // Continuously receive CPU usage data and forward to the frontend
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String cpuData;
            while ((cpuData = reader.readLine()) != null) {
                messagingTemplate.convertAndSend("/topic/cpuUsage", cpuData);
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


