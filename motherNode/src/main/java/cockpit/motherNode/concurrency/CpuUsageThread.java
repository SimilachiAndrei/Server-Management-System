package cockpit.motherNode.concurrency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

@Setter
@Getter

public class CpuUsageThread implements Runnable {

    private Socket socket;
    private String address;
    private Integer port;
    private String name;
    private String jwt;
    private boolean success = false;
    private final SimpMessagingTemplate messagingTemplate;
    ObjectMapper mapper = new ObjectMapper();

    public CpuUsageThread(String address, Integer port, String name, String jwt, SimpMessagingTemplate messagingTemplate) {
        this.address = address;
        this.port = port;
        this.name = name;
        this.jwt = jwt;
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
            while (!socket.isClosed()) {
                int messageLength = inputStream.readInt();
                byte[] data = new byte[messageLength];
                inputStream.read(data);
                String response = new String(data);
                JsonNode json = mapper.readTree(response);
                final String path = "/topic/cpuUsage/".concat(this.jwt).concat("/").concat(this.name);
                messagingTemplate.convertAndSend(path, json.toString());
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


