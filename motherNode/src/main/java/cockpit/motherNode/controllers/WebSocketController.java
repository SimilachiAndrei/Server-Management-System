package cockpit.motherNode.controllers;

import cockpit.motherNode.services.ConnectionService;
import cockpit.motherNode.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class WebSocketController {

    @Autowired
    private ConcurrentHashMap<String, ConnectionService> connectionManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MessageMapping("/sendInput")
    public void sendInput(Map<String, String> payload) throws Exception {
        String input = payload.get("input");
        String jwt = payload.get("jwt");

        System.out.println("Received input: " + input + " for user: " + jwt);

        ConnectionService connectionService = getConnectionServiceByJwt(jwt);
        if (connectionService != null) {
            Socket childNodeSocket = connectionService.getCommandThread().getSocket();
            if (childNodeSocket != null && !childNodeSocket.isClosed()) {
                OutputStream outputStream = childNodeSocket.getOutputStream();
                outputStream.write(input.getBytes());
                outputStream.flush();
            }
        }
    }

    @MessageMapping("/terminateTerminal")
    public void terminateTerminal(Map<String, String> payload) throws Exception {
        String jwt = payload.get("jwt");
        ConnectionService conn = connectionManager.get(jwt);
        conn.disconnect();
        connectionManager.remove(jwt);
    }

    private ConnectionService getConnectionServiceByJwt(String jwt) {
        for (Map.Entry<String, ConnectionService> entry : connectionManager.entrySet()) {
            if (entry.getKey().equals(jwt)) {
                return entry.getValue();
            }
        }
        return null;
    }
}



