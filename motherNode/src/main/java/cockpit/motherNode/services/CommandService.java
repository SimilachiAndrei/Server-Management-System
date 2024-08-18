package cockpit.motherNode.services;

import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.Socket;

@Setter
@Getter

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
        try {
            socket = new Socket(address, port);
            success = true;
        } catch (IOException exception) {
            success = false;
        }
    }

    public String sendCommand(String command) {
        try {
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            byte[] commandBytes = command.getBytes();
            outputStream.writeInt(commandBytes.length);
            outputStream.write(commandBytes);
            outputStream.flush();
            int commandSize = inputStream.readInt();
            byte[] commandResponse = new byte[commandSize];
            inputStream.read(commandResponse);
            String response = new String(commandResponse);
            System.out.println("Response : " + response);

            return response;
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
