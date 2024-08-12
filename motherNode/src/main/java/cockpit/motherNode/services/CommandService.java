package cockpit.motherNode.services;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;

@Setter
@Getter

@Service
public class CommandService {
    private String address;
    private Integer port;
    private Socket socket;
    private boolean success = false;

    public CommandService(String address, Integer port) {
        this.address = address;
        this.port = port;
    }

    public void connect() {
        try (Socket socket = new Socket(address, port)) {
            success = true;
        } catch (IOException exception) {
            success = false;
        }
    }

    public String sendCommand(String command) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(command);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while (null != (line = in.readLine())) {
                response.append(line).append("\n");
            }
            return response.toString();
        } catch (IOException exception) {
            return "Failed to send command: " + exception.getMessage();
        }
    }

    public void stop()
    {
        try{
            socket.close();
        }
        catch (IOException exception)
        {
            //handle error
        }
    }
}
